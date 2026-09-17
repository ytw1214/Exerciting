package com.exerciting.Exerciting.User.Auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 로그인 → 재발급 → 재사용 탐지까지 "진짜 흐름"을 끝까지 확인하는 통합 테스트.
 * 재사용 탐지 시 토큰 삭제가 실제로 커밋되는지 보려고 클래스에 @Transactional을 붙이지 않았다.
 * 대신 테스트마다 랜덤 아이디를 써서 서로 부딪히지 않게 한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    private static final String COOKIE = "REFRESH_TOKEN";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 → 재발급 성공 → 옛 토큰 재사용 시 401 → 이후 새 토큰도 무효")
    void rotationAndReuseDetection() throws Exception {
        String userId = signUpRandomUser();
        MvcResult login = login(userId, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();
        String firstRefresh = refreshCookieOf(login);

        // 1) 정상 재발급: 새 refresh 쿠키가 오고, 옛날 것과 다르다
        MvcResult reissued = mockMvc.perform(post("/user/reissue").cookie(new Cookie(COOKIE, firstRefresh)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();
        String secondRefresh = refreshCookieOf(reissued);
        assertThat(secondRefresh).isNotBlank().isNotEqualTo(firstRefresh);

        // 2) 옛날 토큰을 다시 쓰면 → 탈취로 판단
        mockMvc.perform(post("/user/reissue").cookie(new Cookie(COOKIE, firstRefresh)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TOKEN_REUSE_DETECTED"));

        // 3) 탈취 감지 후에는 정상 사용자 토큰도 무효 (다시 로그인해야 함)
        mockMvc.perform(post("/user/reissue").cookie(new Cookie(COOKIE, secondRefresh)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    @DisplayName("refresh 토큰을 Authorization 헤더에 넣으면 401, access 토큰이면 200")
    void refreshTokenIsNotAccessToken() throws Exception {
        String userId = signUpRandomUser();
        MvcResult login = login(userId, PASSWORD).andExpect(status().isOk()).andReturn();
        String accessToken = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
        String refreshToken = refreshCookieOf(login);

        mockMvc.perform(get("/user/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));

        mockMvc.perform(get("/user/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    @DisplayName("없는 아이디와 틀린 비밀번호는 똑같은 401 응답을 받는다")
    void loginFailuresLookTheSame() throws Exception {
        String userId = signUpRandomUser();

        login(userId, "WrongPassword1!")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));

        login("nobody_" + shortId(), PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
    }

    @Test
    @DisplayName("쿠키 없이 재발급을 요청하면 401")
    void reissueWithoutCookie() throws Exception {
        mockMvc.perform(post("/user/reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    // ===== 도우미 메서드 =====

    private String signUpRandomUser() throws Exception {
        String id = shortId();
        Map<String, String> body = Map.of(
                "userId", "u" + id,          // 9자 (4~20자 규칙)
                "pw", PASSWORD,
                "nickname", "n" + id,        // 9자 (2~10자 규칙)
                "name", "테스터",
                "email", id + "@test.com");
        mockMvc.perform(post("/user/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
        return "u" + id;
    }

    private org.springframework.test.web.servlet.ResultActions login(String userId, String password) throws Exception {
        return mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("userId", userId, "password", password))));
    }

    /** Set-Cookie 헤더에서 REFRESH_TOKEN 값만 꺼낸다. 예) "REFRESH_TOKEN=abc; Path=/user; ..." → "abc" */
    private String refreshCookieOf(MvcResult result) {
        return result.getResponse().getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(header -> header.startsWith(COOKIE + "="))
                .map(header -> header.substring((COOKIE + "=").length(), header.indexOf(';')))
                .findFirst()
                .orElseThrow(() -> new AssertionError("REFRESH_TOKEN 쿠키가 응답에 없습니다"));
    }

    private String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}

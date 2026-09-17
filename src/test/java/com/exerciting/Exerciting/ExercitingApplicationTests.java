package com.exerciting.Exerciting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExercitingApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("스프링 컨텍스트가 정상적으로 로드된다")
    void contextLoads() {
    }

    private Map<String, Object> signUpBody(String userId, String nickname, String email) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("pw", "Password123!");
        body.put("nickname", nickname);
        body.put("name", "홍길동");
        body.put("email", email);
        return body;
    }

    @Nested
    @DisplayName("POST /user/signup - 회원가입 (permitAll)")
    class SignupTest {

        @Test
        @DisplayName("정상 입력 시 가입 정보 DTO를 반환한다")
        void signup_success() throws Exception {
            mockMvc.perform(post("/user/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    signUpBody("testuser01", "테스터", "test@exerciting.com"))))
                    .andExpect(status().is2xxSuccessful())
                    .andExpect(jsonPath("$.userId").exists());
        }

        @Test
        @DisplayName("동일한 userId로 두 번 가입하면 두 번째는 실패한다")
        void signup_fail_duplicate() throws Exception {
            String json = objectMapper.writeValueAsString(
                    signUpBody("sameuser", "중복닉", "dup@test.com"));

            mockMvc.perform(post("/user/signup")
                            .contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().is2xxSuccessful());

            mockMvc.perform(post("/user/signup")
                            .contentType(MediaType.APPLICATION_JSON).content(json))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/games - 경기 조회 (permitAll)")
    class GameGetTest {

        @Test
        @DisplayName("파라미터 없이 요청하면 2xx 반환한다")
        void getGames_noParams() throws Exception {
            mockMvc.perform(get("/api/v1/games"))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("gameStatus=BEFORE 파라미터로 요청하면 2xx 반환한다")
        void getGames_withGameStatus() throws Exception {
            mockMvc.perform(get("/api/v1/games").param("gameStatus", "BEFORE"))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("sportType=BASEBALL 파라미터로 요청하면 2xx 반환한다")
        void getGames_withSportType() throws Exception {
            mockMvc.perform(get("/api/v1/games").param("sportType", "BASEBALL"))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @DisplayName("gameStatus와 sportType 동시 파라미터 요청이 정상 처리된다")
        void getGames_withBothParams() throws Exception {
            mockMvc.perform(get("/api/v1/games")
                            .param("gameStatus", "FINISHED")
                            .param("sportType", "BASEBALL"))
                    .andExpect(status().is2xxSuccessful());
        }
    }

    @Nested
    @DisplayName("인증이 필요한 엔드포인트")
    class AuthRequiredTest {

        @Test
        @DisplayName("인증 없이 매칭 생성을 요청하면 401을 반환한다")
        void createMatching_withoutAuth() throws Exception {
            Map<String, Object> body = new HashMap<>();
            body.put("title", "축구 한 판");
            body.put("description", "설명");
            body.put("maxPerson", 10);
            body.put("meetTime", LocalDateTime.now().plusDays(1).toString());
            body.put("gameId", 1L);

            mockMvc.perform(post("/api/v1/matching")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증 없이 매칭 목록을 조회하면 401을 반환한다")
        void getMatching_withoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/matching"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/Matching - 매칭 생성 (인증됨)")
    @WithMockUser(username = "host01")
    class MatchingCreateTest {

        private Map<String, Object> matchingBody(String title, int maxPerson, LocalDateTime meetTime) {
            Map<String, Object> body = new HashMap<>();
            body.put("title", title);
            body.put("description", "설명");
            body.put("maxPerson", maxPerson);
            body.put("meetTime", meetTime.toString());
            body.put("gameId", 1L);
            return body;
        }

        @Test
        @DisplayName("meetTime이 과거이면 4xx 응답")
        void createMatching_fail_pastTime() throws Exception {
            mockMvc.perform(post("/api/v1/Matching")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    matchingBody("과거 매칭", 5, LocalDateTime.now().minusHours(1)))))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("maxPerson이 1이면 4xx 응답")
        void createMatching_fail_lowMaxPerson() throws Exception {
            mockMvc.perform(post("/api/v1/Matching")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    matchingBody("혼자 매칭", 1, LocalDateTime.now().plusDays(1)))))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("title이 공백이면 4xx 응답")
        void createMatching_fail_blankTitle() throws Exception {
            mockMvc.perform(post("/api/v1/Matching")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    matchingBody("   ", 5, LocalDateTime.now().plusDays(1)))))
                    .andExpect(status().is4xxClientError());
        }
    }
}
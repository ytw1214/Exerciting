package com.exerciting.Exerciting;

import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MatchingRepository matchingRepository;
	@Test
	@DisplayName("스프링 컨텍스트가 정상적으로 로드된다")
	void contextLoads() {
		// 빈 테스트: 컨텍스트 뜨면 통과
	}

	@Test
	@DisplayName("GET / - 헬스체크 엔드포인트가 200을 반환한다")
	void healthCheck() throws Exception {
		mockMvc.perform(get("/"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string("Yammy!~"));
	}

	@Nested
	@DisplayName("POST /signup - 회원가입")
	class SignupTest {

		@Test
		@DisplayName("정상 입력 시 userId(PK)를 반환하고 200 응답한다")
		void signup_success() throws Exception {
			Map<String, String> body = Map.of(
					"userId", "testuser01",
					"pw", "password123",
					"nickname", "테스터",
					"name", "홍길동",
					"email", "test@exerciting.com"
			);

			mockMvc.perform(post("/signup")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(body)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isNumber()); // 반환값이 Long id
		}

		@Test
		@DisplayName("동일한 요청 두 번 보내도 각각 다른 id가 발급된다 (중복 검증 로직 없음 확인)")
		void signup_duplicateAllowed() throws Exception {
			Map<String, String> body = Map.of(
					"userId", "sameuser",
					"pw", "pw",
					"nickname", "중복닉",
					"name", "중복이름",
					"email", "dup@test.com"
			);
			String json = objectMapper.writeValueAsString(body);

			String result1 = mockMvc.perform(post("/signup")
							.contentType(MediaType.APPLICATION_JSON).content(json))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString();

			String result2 = mockMvc.perform(post("/signup")
							.contentType(MediaType.APPLICATION_JSON).content(json))
					.andExpect(status().isOk())
					.andReturn().getResponse().getContentAsString();

			org.assertj.core.api.Assertions.assertThat(result1).isNotEqualTo(result2);
		}
	}
	@Nested
	@DisplayName("POST /api/v1/Matching - 매칭 생성")
	class MatchingCreateTest {

		@BeforeEach
		void insertHostUser() throws Exception {
			Map<String, String> body = Map.of(
					"userId", "host01",
					"pw", "pw",
					"nickname", "방장",
					"name", "방장이름",
					"email", "host@test.com"
			);
			mockMvc.perform(post("/signup")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(body)));
		}

		@Test
		@DisplayName("정상 요청 시 매칭 id를 반환하고 200 응답한다")
		void createMatching_success() throws Exception {
			Map<String, Object> body = Map.of(
					"title", "축구 한 판 하실 분",
					"description", "같이 뛰어요",
					"maxPerson", 10,
					"meetTime", LocalDateTime.now().plusDays(1).toString()
			);

			mockMvc.perform(post("/api/v1/Matching")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(body)))
					.andDo(print())
					.andExpect(status().isOk())
					.andExpect(jsonPath("$").isNumber());
		}

		@Test
		@DisplayName("meetTime이 과거이면 500 응답 (InvalidTimeException - ExceptionHandler 미설정)")
		void createMatching_fail_pastTime() throws Exception {
			Map<String, Object> body = Map.of(
					"title", "과거 매칭",
					"description", "설명",
					"maxPerson", 5,
					"meetTime", LocalDateTime.now().minusHours(1).toString()
			);

			mockMvc.perform(post("/api/v1/Matching")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(body)))
					.andDo(print())
					.andExpect(status().is5xxServerError());
		}

		@Test
		@DisplayName("maxPerson이 1이면 500 응답 (InvalidInputException)")
		void createMatching_fail_lowMaxPerson() throws Exception {
			Map<String, Object> body = Map.of(
					"title", "혼자 매칭",
					"description", "설명",
					"maxPerson", 1,
					"meetTime", LocalDateTime.now().plusDays(1).toString()
			);

			mockMvc.perform(post("/api/v1/Matching")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(body)))
					.andDo(print())
					.andExpect(status().is5xxServerError());
		}

		@Test
		@DisplayName("title이 공백이면 500 응답 (InvalidInputException)")
		void createMatching_fail_blankTitle() throws Exception {
			Map<String, Object> body = Map.of(
					"title", "   ",
					"description", "설명",
					"maxPerson", 5,
					"meetTime", LocalDateTime.now().plusDays(1).toString()
			);

			mockMvc.perform(post("/api/v1/Matching")
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(body)))
					.andDo(print())
					.andExpect(status().is5xxServerError());
		}
	}

	@Nested
	@DisplayName("GET /api/v1/Matching - 매칭 목록 조회")
	class MatchingGetTest {

		@Test
		@DisplayName("매칭이 없으면 204 No Content를 반환한다")
		void getMatching_empty() throws Exception {
			mockMvc.perform(get("/api/v1/Matching"))
					.andDo(print())
					.andExpect(status().isNoContent());
		}
	}

	@Nested
	@DisplayName("GET /api/v1/games - 경기 조회")
	class GameGetTest {

		@Test
		@DisplayName("데이터 없을 때 파라미터 없이 요청하면 204 반환한다")
		void getGames_empty_noParams() throws Exception {
			mockMvc.perform(get("/api/v1/games"))
					.andDo(print())
					.andExpect(status().isNoContent());
		}

		@Test
		@DisplayName("gameStatus=BEFORE 파라미터로 요청하면 204 또는 200 반환한다")
		void getGames_withGameStatus() throws Exception {
			mockMvc.perform(get("/api/v1/games")
							.param("gameStatus", "BEFORE"))
					.andDo(print())
					.andExpect(status().is2xxSuccessful());
		}

		@Test
		@DisplayName("sportType=BASEBALL 파라미터로 요청하면 204 또는 200 반환한다")
		void getGames_withSportType() throws Exception {
			mockMvc.perform(get("/api/v1/games")
							.param("sportType", "BASEBALL"))
					.andDo(print())
					.andExpect(status().is2xxSuccessful());
		}

		@Test
		@DisplayName("gameStatus와 sportType 동시 파라미터 요청이 정상 처리된다")
		void getGames_withBothParams() throws Exception {
			mockMvc.perform(get("/api/v1/games")
							.param("gameStatus", "FINISHED")
							.param("sportType", "BASEBALL"))
					.andDo(print())
					.andExpect(status().is2xxSuccessful());
		}
	}
}
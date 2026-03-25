package com.exerciting.Exerciting.Matching;

import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingCustomCond;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.InvalidTimeException;
import com.exerciting.Exerciting.Exception.UnauthorizedUserException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/**
 * MatchingService 단위 테스트 (Mockito 기반)
 * - DB 없이 의존성 격리
 * - 실제 코드 시그니처: createMatching(MatchingRequestDto dto, Long hostId)
 */
@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private MatchingRepository matchingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private com.exerciting.Exerciting.Domain.game.repository.GameRepository gameRepository;

    @InjectMocks
    private MatchingService matchingService;

    private User mockHost;

    @BeforeEach
    void setUp() {
        mockHost = User.builder()
                .userId("host01").pw("pw").nickname("방장닉").name("방장").email("host@test.com")
                .build();
    }

    @Nested
    @DisplayName("createMatching - 매칭 생성")
    class CreateMatchingTest {

        @Test
        @DisplayName("정상 입력이면 matchingRepository.save()를 1회 호출한다")
        void createMatching_success() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("축구 한 판").description("같이 뛰어요")
                    .maxPerson(10).meetTime(LocalDateTime.now().plusDays(1)).build();

            Matching savedMatching = Matching.builder()
                    .title(dto.getTitle()).description(dto.getDescription())
                    .maxPerson(dto.getMaxPerson()).currentPerson(1)
                    .user(mockHost).meetTime(dto.getMeetTime()).build();

            given(userRepository.findById(1L)).willReturn(Optional.of(mockHost));
            given(matchingRepository.save(any(Matching.class))).willReturn(savedMatching);

            matchingService.createMatching(dto, 1L);

            then(matchingRepository).should(times(1)).save(any(Matching.class));
        }

        @Test
        @DisplayName("meetTime이 과거이면 InvalidTimeException 발생 - Repository 호출 없음")
        void createMatching_fail_pastTime() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("과거 매칭").description("설명")
                    .maxPerson(5).meetTime(LocalDateTime.now().minusHours(1)).build();

            assertThatThrownBy(() -> matchingService.createMatching(dto, 1L))
                    .isInstanceOf(InvalidTimeException.class);

            then(matchingRepository).should(never()).save(any());
            then(userRepository).should(never()).findById(any());
        }

        @Test
        @DisplayName("maxPerson이 1이면 InvalidInputException 발생")
        void createMatching_fail_maxPersonUnder2() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("혼자 매칭").description("설명")
                    .maxPerson(1).meetTime(LocalDateTime.now().plusDays(1)).build();

            assertThatThrownBy(() -> matchingService.createMatching(dto, 1L))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("title이 공백만이면 InvalidInputException 발생")
        void createMatching_fail_blankTitle() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("   ").description("설명")
                    .maxPerson(5).meetTime(LocalDateTime.now().plusDays(1)).build();

            assertThatThrownBy(() -> matchingService.createMatching(dto, 1L))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("title이 null이면 InvalidInputException 발생")
        void createMatching_fail_nullTitle() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title(null).description("설명")
                    .maxPerson(5).meetTime(LocalDateTime.now().plusDays(1)).build();

            assertThatThrownBy(() -> matchingService.createMatching(dto, 1L))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("존재하지 않는 userId이면 InvalidInputException 발생")
        void createMatching_fail_userNotFound() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("정상 매칭").description("설명")
                    .maxPerson(5).meetTime(LocalDateTime.now().plusDays(1)).build();

            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> matchingService.createMatching(dto, 999L))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessageContaining("존재하지 않는 사용자");
        }
    }

    @Nested
    @DisplayName("deleteMatching - 매칭 삭제")
    class DeleteMatchingTest {

        @Test
        @DisplayName("존재하지 않는 매칭 삭제 시 EntityNotFoundException 발생")
        void deleteMatching_fail_matchingNotFound() {
            given(matchingRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> matchingService.deleteMatching(999L, 1L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("해당 모임이 존재하지 않습니다");
        }

        @Test
        @DisplayName("소유자가 아닌 userId로 삭제 시도 시 UnauthorizedUserException 발생")
        void deleteMatching_fail_unauthorized() {
            // mockHost.getId() == null, currentUserId == 99L → null != 99L → 권한 없음
            Matching matching = Matching.builder()
                    .title("남의 매칭").maxPerson(5).currentPerson(1)
                    .user(mockHost).meetTime(LocalDateTime.now().plusDays(1)).build();

            given(matchingRepository.findById(1L)).willReturn(Optional.of(matching));

            assertThatThrownBy(() -> matchingService.deleteMatching(1L, 99L))
                    .isInstanceOf(UnauthorizedUserException.class)
                    .hasMessageContaining("잘못된 접근");
        }
    }

    @Nested
    @DisplayName("updateMatching - 매칭 수정")
    class UpdateMatchingTest {

        @Test
        @DisplayName("수정 시 maxPerson < 2이면 엔티티 내부 검증에서 InvalidInputException 발생")
        void updateMatching_fail_lowMaxPerson() {
            Matching matching = Matching.builder()
                    .title("수정 대상").maxPerson(5).currentPerson(1)
                    .user(mockHost).meetTime(LocalDateTime.now().plusDays(1)).build();

            MatchingRequestDto changedDto = MatchingRequestDto.builder()
                    .title("수정된 제목").description("설명")
                    .maxPerson(1).meetTime(LocalDateTime.now().plusDays(2)).build();

            given(matchingRepository.findById(1L)).willReturn(Optional.of(matching));

            // mockHost.getId() == null, currentUserId == null → null.equals(null) == true → verify 통과
            assertThatThrownBy(() -> matchingService.updateMatching(1L, null, changedDto))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessageContaining("최소 2명");
        }
    }

    @Nested
    @DisplayName("searchDetailMatching - 조건 검색")
    class SearchDetailMatchingTest {

        @Test
        @DisplayName("인증된 유저이면 matchingRepository.search()를 호출하고 결과를 반환한다")
        void searchDetailMatching_success() {
            MatchingCustomCond cond = new MatchingCustomCond("축구", null, null, null);

            given(userRepository.existsById(1L)).willReturn(true);
            given(matchingRepository.search(cond)).willReturn(List.of());

            var result = matchingService.searchDetailMatching(cond, 1L);

            assertThat(result).isNotNull();
            then(matchingRepository).should(times(1)).search(cond);
        }

        @Test
        @DisplayName("미인증 유저이면 UnauthorizedUserException 발생, search() 미호출")
        void searchDetailMatching_fail_unauthenticated() {
            MatchingCustomCond cond = new MatchingCustomCond(null, null, null, null);
            given(userRepository.existsById(999L)).willReturn(false);

            assertThatThrownBy(() -> matchingService.searchDetailMatching(cond, 999L))
                    .isInstanceOf(UnauthorizedUserException.class)
                    .hasMessageContaining("인증된 사용자만");

            then(matchingRepository).should(never()).search(any());
        }
    }
}
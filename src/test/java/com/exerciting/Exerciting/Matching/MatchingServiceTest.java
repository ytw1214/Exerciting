package com.exerciting.Exerciting.Matching;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.entity.MatchingStatus;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingCustomCond;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.InvalidTimeException;
import com.exerciting.Exerciting.Exception.MatchingNotFoundException;
import com.exerciting.Exerciting.Exception.UnauthorizedUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    private static final Long HOST_ID = 1L;
    private static final Long OTHER_USER_ID = 99L;

    @Mock
    private MatchingRepository matchingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private MatchingChatRoomRepository matchingChatRoomRepository;
    @Mock
    private MatchingParticipantRepository matchingParticipantRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MatchingService matchingService;

    private User mockHost;

    @BeforeEach
    void setUp() {
        mockHost = User.builder()
                .userId("host01").pw("pw").nickname("방장닉").name("방장").email("host@test.com")
                .build();
        // User에 setter가 없고 빌더로는 id를 넣을 수 없어,
        // 소유자 검증(matching.getUser().getId().equals(...)) 테스트를 위해 리플렉션으로 주입한다.
        ReflectionTestUtils.setField(mockHost, "id", HOST_ID);
    }

    private Matching createMatchingOwnedByHost(String title) {
        return Matching.builder()
                .title(title)
                .maxPerson(5)
                .user(mockHost)
                .meetTime(LocalDateTime.now().plusDays(1))
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
                    .maxPerson(10).meetTime(LocalDateTime.now().plusDays(1))
                    .gameId(1L)
                    .build();

            Matching savedMatching = Matching.builder()
                    .title(dto.getTitle()).description(dto.getDescription())
                    .maxPerson(dto.getMaxPerson())
                    .user(mockHost).meetTime(dto.getMeetTime()).build();

            given(userRepository.findById(HOST_ID)).willReturn(Optional.of(mockHost));
            given(gameRepository.findById(1L)).willReturn(Optional.of(mock(Game.class)));
            given(matchingRepository.save(any(Matching.class))).willReturn(savedMatching);

            matchingService.createMatching(dto, HOST_ID);

            then(matchingRepository).should(times(1)).save(any(Matching.class));
        }

        @Test
        @DisplayName("매칭 생성 시 채팅방과 호스트 참가 기록도 함께 만든다")
        void createMatching_createsChatRoomAndParticipant() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("농구 한 판").description("설명")
                    .maxPerson(6).meetTime(LocalDateTime.now().plusDays(1))
                    .gameId(1L)
                    .build();

            Matching savedMatching = createMatchingOwnedByHost("농구 한 판");

            given(userRepository.findById(HOST_ID)).willReturn(Optional.of(mockHost));
            given(gameRepository.findById(1L)).willReturn(Optional.of(mock(Game.class)));
            given(matchingRepository.save(any(Matching.class))).willReturn(savedMatching);

            matchingService.createMatching(dto, HOST_ID);

            then(matchingChatRoomRepository).should(times(1)).save(any());
            then(matchingParticipantRepository).should(times(1)).save(any());
        }

        @Test
        @DisplayName("meetTime이 과거이면 InvalidTimeException 발생 - Repository 호출 없음")
        void createMatching_fail_pastTime() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("과거 매칭").description("설명")
                    .maxPerson(5).meetTime(LocalDateTime.now().minusHours(1))
                    .gameId(1L)
                    .build();

            assertThatThrownBy(() -> matchingService.createMatching(dto, HOST_ID))
                    .isInstanceOf(InvalidTimeException.class);

            then(matchingRepository).should(never()).save(any());
            then(userRepository).should(never()).findById(any());
        }

        @Test
        @DisplayName("maxPerson이 1이면 InvalidInputException 발생")
        void createMatching_fail_maxPersonUnder2() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("혼자 매칭").description("설명")
                    .maxPerson(1).meetTime(LocalDateTime.now().plusDays(1))
                    .gameId(1L)
                    .build();

            assertThatThrownBy(() -> matchingService.createMatching(dto, HOST_ID))
                    .isInstanceOf(InvalidInputException.class);

            then(matchingRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("title이 공백만이면 InvalidInputException 발생")
        void createMatching_fail_blankTitle() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("   ").description("설명")
                    .maxPerson(5).meetTime(LocalDateTime.now().plusDays(1))
                    .gameId(1L)
                    .build();

            assertThatThrownBy(() -> matchingService.createMatching(dto, HOST_ID))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("title이 null이면 InvalidInputException 발생")
        void createMatching_fail_nullTitle() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title(null).description("설명")
                    .maxPerson(5).meetTime(LocalDateTime.now().plusDays(1))
                    .gameId(1L)
                    .build();

            assertThatThrownBy(() -> matchingService.createMatching(dto, HOST_ID))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("존재하지 않는 userId이면 InvalidInputException 발생")
        void createMatching_fail_userNotFound() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("정상 매칭").description("설명")
                    .maxPerson(5).meetTime(LocalDateTime.now().plusDays(1))
                    .gameId(1L)
                    .build();

            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> matchingService.createMatching(dto, 999L))
                    .isInstanceOf(InvalidInputException.class);

            then(matchingRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("존재하지 않는 gameId이면 InvalidInputException 발생")
        void createMatching_fail_gameNotFound() {
            MatchingRequestDto dto = MatchingRequestDto.builder()
                    .title("정상 매칭").description("설명")
                    .maxPerson(5).meetTime(LocalDateTime.now().plusDays(1))
                    .gameId(999L)
                    .build();

            given(userRepository.findById(HOST_ID)).willReturn(Optional.of(mockHost));
            given(gameRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> matchingService.createMatching(dto, HOST_ID))
                    .isInstanceOf(InvalidInputException.class);

            then(matchingRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteMatching - 매칭 취소(소프트 삭제)")
    class DeleteMatchingTest {

        @Test
        @DisplayName("존재하지 않는 매칭 삭제 시 MatchingNotFoundException 발생")
        void deleteMatching_fail_matchingNotFound() {
            given(matchingRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> matchingService.deleteMatching(999L, HOST_ID))
                    .isInstanceOf(MatchingNotFoundException.class);
        }

        @Test
        @DisplayName("소유자가 아닌 userId로 삭제 시도 시 UnauthorizedUserException 발생")
        void deleteMatching_fail_unauthorized() {
            Matching matching = createMatchingOwnedByHost("남의 매칭");
            given(matchingRepository.findById(1L)).willReturn(Optional.of(matching));

            assertThatThrownBy(() -> matchingService.deleteMatching(1L, OTHER_USER_ID))
                    .isInstanceOf(UnauthorizedUserException.class);

            assertThat(matching.getStatus()).isNotEqualTo(MatchingStatus.CANCELLED);
        }

        @Test
        @DisplayName("삭제 시 실제로 지우지 않고 상태를 CANCELLED로 변경한다")
        void deleteMatching_softDelete() {
            Matching matching = createMatchingOwnedByHost("내 매칭");
            given(matchingRepository.findById(1L)).willReturn(Optional.of(matching));

            matchingService.deleteMatching(1L, HOST_ID);

            assertThat(matching.getStatus()).isEqualTo(MatchingStatus.CANCELLED);
            then(matchingRepository).should(never()).delete(any(Matching.class));
        }
    }

    @Nested
    @DisplayName("updateMatching - 매칭 수정")
    class UpdateMatchingTest {

        @Test
        @DisplayName("수정 시 maxPerson < 2이면 엔티티 내부 검증에서 InvalidInputException 발생")
        void updateMatching_fail_lowMaxPerson() {
            Matching matching = createMatchingOwnedByHost("수정 대상");

            MatchingRequestDto changedDto = MatchingRequestDto.builder()
                    .title("수정된 제목").description("설명")
                    .maxPerson(1).meetTime(LocalDateTime.now().plusDays(2)).build();

            given(matchingRepository.findById(1L)).willReturn(Optional.of(matching));

            assertThatThrownBy(() -> matchingService.updateMatching(1L, HOST_ID, changedDto))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("소유자가 아니면 UnauthorizedUserException 발생")
        void updateMatching_fail_unauthorized() {
            Matching matching = createMatchingOwnedByHost("수정 대상");

            MatchingRequestDto changedDto = MatchingRequestDto.builder()
                    .title("수정된 제목").description("설명")
                    .maxPerson(8).meetTime(LocalDateTime.now().plusDays(2)).build();

            given(matchingRepository.findById(1L)).willReturn(Optional.of(matching));

            assertThatThrownBy(() -> matchingService.updateMatching(1L, OTHER_USER_ID, changedDto))
                    .isInstanceOf(UnauthorizedUserException.class);
        }

        @Test
        @DisplayName("정상 수정이면 엔티티 값이 변경된다")
        void updateMatching_success() {
            Matching matching = createMatchingOwnedByHost("수정 전");
            LocalDateTime newMeetTime = LocalDateTime.now().plusDays(3);

            MatchingRequestDto changedDto = MatchingRequestDto.builder()
                    .title("수정 후").description("바뀐 설명")
                    .maxPerson(8).meetTime(newMeetTime).build();

            given(matchingRepository.findById(1L)).willReturn(Optional.of(matching));

            matchingService.updateMatching(1L, HOST_ID, changedDto);

            assertThat(matching.getTitle()).isEqualTo("수정 후");
            assertThat(matching.getMaxPerson()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("searchDetailMatching - 조건 검색")
    class SearchDetailMatchingTest {

        @Test
        @DisplayName("인증된 유저이면 matchingRepository.search()를 호출하고 결과를 반환한다")
        void searchDetailMatching_success() {
            MatchingCustomCond cond = new MatchingCustomCond("축구", null, null, null);

            given(userRepository.existsById(HOST_ID)).willReturn(true);
            given(matchingRepository.search(cond)).willReturn(List.of());

            var result = matchingService.searchDetailMatching(cond, HOST_ID);

            assertThat(result).isNotNull();
            then(matchingRepository).should(times(1)).search(cond);
        }

        @Test
        @DisplayName("미인증 유저이면 UnauthorizedUserException 발생, search() 미호출")
        void searchDetailMatching_fail_unauthenticated() {
            MatchingCustomCond cond = new MatchingCustomCond(null, null, null, null);
            given(userRepository.existsById(999L)).willReturn(false);

            assertThatThrownBy(() -> matchingService.searchDetailMatching(cond, 999L))
                    .isInstanceOf(UnauthorizedUserException.class);

            then(matchingRepository).should(never()).search(any());
        }
    }
}
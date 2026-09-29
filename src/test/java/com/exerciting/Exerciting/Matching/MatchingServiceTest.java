package com.exerciting.Exerciting.Matching;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingCreateResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingStatusResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.entity.MatchingStatus;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingCustomCond;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.ParticipantStatus;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Exception.HostCannotLeaveException;
import com.exerciting.Exerciting.Exception.InvalidCapacityException;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.InvalidTimeException;
import com.exerciting.Exerciting.Exception.MatchingFullException;
import com.exerciting.Exerciting.Exception.MatchingNotFoundException;
import com.exerciting.Exerciting.Exception.MatchingNotRecruitingException;
import com.exerciting.Exerciting.Exception.NotParticipantException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

/**
 * MatchingService 단위 테스트.
 * 락이 실제로 동시 요청을 막는지는 MatchingJoinConcurrencyTest(통합 테스트)가 확인하고,
 * 여기서는 상태·정원 규칙과 권한 규칙만 빠르게 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    private static final Long HOST_ID = 1L;
    private static final Long GUEST_ID = 2L;
    private static final Long OTHER_USER_ID = 99L;
    private static final Long MATCHING_ID = 1L;

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

    private User host;
    private User guest;

    @BeforeEach
    void setUp() {
        host = user("host01", HOST_ID);
        guest = user("guest01", GUEST_ID);
    }

    // User·Matching에는 id setter가 없어 리플렉션으로 넣는다 (소유자 검증이 id로 비교하기 때문)
    private User user(String userId, Long id) {
        User user = User.builder()
                .userId(userId).pw("pw").nickname(userId).name("이름").email(userId + "@test.com")
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Matching matchingOwnedByHost(int maxPerson) {
        Matching matching = Matching.builder()
                .title("매칭")
                .maxPerson(maxPerson)
                .user(host)
                .meetTime(LocalDateTime.now().plusDays(1))
                .build();
        ReflectionTestUtils.setField(matching, "id", MATCHING_ID);
        return matching;
    }

    private MatchingRequestDto request(String title, int maxPerson, LocalDateTime meetTime) {
        return MatchingRequestDto.builder()
                .title(title).description("설명")
                .maxPerson(maxPerson).meetTime(meetTime)
                .gameId(1L)
                .build();
    }

    @Nested
    @DisplayName("createMatching - 매칭 생성")
    class CreateMatchingTest {

        @Test
        @DisplayName("매칭·채팅방·호스트 참가 행을 한 번씩 저장하고 현재 인원 1명으로 응답한다")
        void createMatching_success() {
            Matching saved = matchingOwnedByHost(10);
            MatchingChatRoom chatRoom = MatchingChatRoom.builder().matching(saved).requester(host).build();
            ReflectionTestUtils.setField(chatRoom, "id", 10L);

            Game game = mock(Game.class);
            given(userRepository.findById(HOST_ID)).willReturn(Optional.of(host));
            given(gameRepository.findById(1L)).willReturn(Optional.of(game));
            given(matchingRepository.save(any(Matching.class))).willReturn(saved);
            given(matchingChatRoomRepository.save(any(MatchingChatRoom.class))).willReturn(chatRoom);

            MatchingCreateResponseDto result =
                    matchingService.createMatching(request("축구 한 판", 10, LocalDateTime.now().plusDays(1)), HOST_ID);

            assertThat(result.matchingId()).isEqualTo(MATCHING_ID);
            assertThat(result.chatRoomId()).isEqualTo(10L);
            assertThat(result.currentPerson()).isEqualTo(1);
            then(matchingRepository).should(times(1)).save(any(Matching.class));
            then(matchingChatRoomRepository).should(times(1)).save(any(MatchingChatRoom.class));
            // 회귀 방지: 호스트 참가 행을 두 번 저장하면 (matching_id, user_id) 유니크 제약에 걸린다
            then(matchingParticipantRepository).should(times(1)).save(any(MatchingParticipant.class));
        }

        @Test
        @DisplayName("meetTime이 과거이면 InvalidTimeException, 저장·조회 없음")
        void createMatching_fail_pastTime() {
            assertThatThrownBy(() -> matchingService.createMatching(
                    request("과거 매칭", 5, LocalDateTime.now().minusHours(1)), HOST_ID))
                    .isInstanceOf(InvalidTimeException.class);

            then(userRepository).should(never()).findById(any());
            then(matchingRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("maxPerson이 1이면 InvalidInputException")
        void createMatching_fail_maxPersonUnder2() {
            assertThatThrownBy(() -> matchingService.createMatching(
                    request("혼자 매칭", 1, LocalDateTime.now().plusDays(1)), HOST_ID))
                    .isInstanceOf(InvalidInputException.class);

            then(matchingRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("title이 공백이거나 null이면 InvalidInputException")
        void createMatching_fail_blankTitle() {
            assertThatThrownBy(() -> matchingService.createMatching(
                    request("   ", 5, LocalDateTime.now().plusDays(1)), HOST_ID))
                    .isInstanceOf(InvalidInputException.class);
            assertThatThrownBy(() -> matchingService.createMatching(
                    request(null, 5, LocalDateTime.now().plusDays(1)), HOST_ID))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("존재하지 않는 gameId이면 InvalidInputException, 매칭 저장 없음")
        void createMatching_fail_gameNotFound() {
            given(userRepository.findById(HOST_ID)).willReturn(Optional.of(host));
            given(gameRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> matchingService.createMatching(
                    request("정상 매칭", 5, LocalDateTime.now().plusDays(1)), HOST_ID))
                    .isInstanceOf(InvalidInputException.class);

            then(matchingRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("joinMatching - 매칭 참가")
    class JoinMatchingTest {

        @Test
        @DisplayName("자리가 있으면 참가 행을 새로 만들고 현재 인원을 1 늘린다")
        void join_success() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));
            given(userRepository.findById(GUEST_ID)).willReturn(Optional.of(guest));
            given(matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED)).willReturn(1L);
            given(matchingParticipantRepository.findByMatchingAndUser(matching, guest)).willReturn(Optional.empty());

            MatchingStatusResponseDto result = matchingService.joinMatching(MATCHING_ID, GUEST_ID);

            assertThat(result.currentPerson()).isEqualTo(2);
            assertThat(matching.getStatus()).isEqualTo(MatchingStatus.RECRUITING);
            then(matchingParticipantRepository).should(times(1)).save(any(MatchingParticipant.class));
        }

        @Test
        @DisplayName("마지막 자리를 채우면 상태가 FULL로 바뀐다")
        void join_lastSeat_becomesFull() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));
            given(userRepository.findById(GUEST_ID)).willReturn(Optional.of(guest));
            given(matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED)).willReturn(4L);
            given(matchingParticipantRepository.findByMatchingAndUser(matching, guest)).willReturn(Optional.empty());

            matchingService.joinMatching(MATCHING_ID, GUEST_ID);

            assertThat(matching.getStatus()).isEqualTo(MatchingStatus.FULL);
        }

        @Test
        @DisplayName("정원이 찼으면 MatchingFullException, 참가 행을 만들지 않는다")
        void join_fail_full() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));
            given(userRepository.findById(GUEST_ID)).willReturn(Optional.of(guest));
            given(matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED)).willReturn(5L);

            assertThatThrownBy(() -> matchingService.joinMatching(MATCHING_ID, GUEST_ID))
                    .isInstanceOf(MatchingFullException.class);

            then(matchingParticipantRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("마감(CLOSED)된 매칭에는 참가할 수 없다")
        void join_fail_closed() {
            Matching matching = matchingOwnedByHost(5);
            matching.close();
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));
            given(userRepository.findById(GUEST_ID)).willReturn(Optional.of(guest));
            given(matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED)).willReturn(1L);

            assertThatThrownBy(() -> matchingService.joinMatching(MATCHING_ID, GUEST_ID))
                    .isInstanceOf(MatchingNotRecruitingException.class);
        }

        @Test
        @DisplayName("나갔던 사용자가 다시 들어오면 새 행 대신 기존 행을 JOINED로 되살린다")
        void join_rejoin() {
            Matching matching = matchingOwnedByHost(5);
            MatchingParticipant left = MatchingParticipant.builder().user(guest).matching(matching).build();
            left.leave();
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));
            given(userRepository.findById(GUEST_ID)).willReturn(Optional.of(guest));
            given(matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED)).willReturn(1L);
            given(matchingParticipantRepository.findByMatchingAndUser(matching, guest)).willReturn(Optional.of(left));

            matchingService.joinMatching(MATCHING_ID, GUEST_ID);

            assertThat(left.getStatus()).isEqualTo(ParticipantStatus.JOINED);
            then(matchingParticipantRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("leaveMatching - 매칭 이탈")
    class LeaveMatchingTest {

        @Test
        @DisplayName("호스트는 나갈 수 없다")
        void leave_fail_host() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));
            given(userRepository.findById(HOST_ID)).willReturn(Optional.of(host));

            assertThatThrownBy(() -> matchingService.leaveMatching(MATCHING_ID, HOST_ID))
                    .isInstanceOf(HostCannotLeaveException.class);
        }

        @Test
        @DisplayName("참가 중(JOINED)이 아니면 NotParticipantException")
        void leave_fail_notParticipant() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));
            given(userRepository.findById(GUEST_ID)).willReturn(Optional.of(guest));
            given(matchingParticipantRepository.findByMatchingAndUserAndStatus(matching, guest, ParticipantStatus.JOINED))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> matchingService.leaveMatching(MATCHING_ID, GUEST_ID))
                    .isInstanceOf(NotParticipantException.class);
        }

        @Test
        @DisplayName("나가면 행을 지우지 않고 LEFT로 바꾸며, FULL이었다면 RECRUITING으로 되돌린다")
        void leave_success() {
            Matching matching = matchingOwnedByHost(2);
            matching.refreshCapacityStatus(2); // FULL
            MatchingParticipant joined = MatchingParticipant.builder().user(guest).matching(matching).build();
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));
            given(userRepository.findById(GUEST_ID)).willReturn(Optional.of(guest));
            given(matchingParticipantRepository.findByMatchingAndUserAndStatus(matching, guest, ParticipantStatus.JOINED))
                    .willReturn(Optional.of(joined));
            given(matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED)).willReturn(1L);

            matchingService.leaveMatching(MATCHING_ID, GUEST_ID);

            assertThat(joined.getStatus()).isEqualTo(ParticipantStatus.LEFT);
            assertThat(matching.getStatus()).isEqualTo(MatchingStatus.RECRUITING);
            then(matchingParticipantRepository).should(never()).delete(any());
        }
    }

    @Nested
    @DisplayName("deleteMatching - 매칭 취소(소프트 삭제)")
    class DeleteMatchingTest {

        @Test
        @DisplayName("존재하지 않는 매칭이면 MatchingNotFoundException")
        void delete_fail_notFound() {
            given(matchingRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> matchingService.deleteMatching(999L, HOST_ID))
                    .isInstanceOf(MatchingNotFoundException.class);
        }

        @Test
        @DisplayName("호스트가 아니면 UnauthorizedUserException(403), 상태는 그대로")
        void delete_fail_notHost() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));

            assertThatThrownBy(() -> matchingService.deleteMatching(MATCHING_ID, OTHER_USER_ID))
                    .isInstanceOf(UnauthorizedUserException.class);

            assertThat(matching.getStatus()).isNotEqualTo(MatchingStatus.CANCELLED);
        }

        @Test
        @DisplayName("삭제 요청은 행을 지우지 않고 CANCELLED로 바꾼다")
        void delete_softDelete() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));

            matchingService.deleteMatching(MATCHING_ID, HOST_ID);

            assertThat(matching.getStatus()).isEqualTo(MatchingStatus.CANCELLED);
            then(matchingRepository).should(never()).delete(any(Matching.class));
        }
    }

    @Nested
    @DisplayName("updateMatching - 매칭 수정")
    class UpdateMatchingTest {

        @Test
        @DisplayName("정원을 2명 미만으로 줄이면 InvalidCapacityException")
        void update_fail_lowMaxPerson() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));

            assertThatThrownBy(() -> matchingService.updateMatching(MATCHING_ID, HOST_ID,
                    request("수정", 1, LocalDateTime.now().plusDays(2))))
                    .isInstanceOf(InvalidCapacityException.class);
        }

        @Test
        @DisplayName("현재 참가 인원보다 정원을 작게 줄일 수 없다")
        void update_fail_belowCurrentCount() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));
            given(matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED)).willReturn(4L);

            assertThatThrownBy(() -> matchingService.updateMatching(MATCHING_ID, HOST_ID,
                    request("수정", 3, LocalDateTime.now().plusDays(2))))
                    .isInstanceOf(InvalidCapacityException.class);
        }

        @Test
        @DisplayName("호스트가 아니면 UnauthorizedUserException")
        void update_fail_notHost() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));

            assertThatThrownBy(() -> matchingService.updateMatching(MATCHING_ID, OTHER_USER_ID,
                    request("수정", 8, LocalDateTime.now().plusDays(2))))
                    .isInstanceOf(UnauthorizedUserException.class);
        }

        @Test
        @DisplayName("정상 수정이면 엔티티 값이 바뀐다")
        void update_success() {
            Matching matching = matchingOwnedByHost(5);
            given(matchingRepository.findByIdWithLock(MATCHING_ID)).willReturn(Optional.of(matching));

            matchingService.updateMatching(MATCHING_ID, HOST_ID, request("수정 후", 8, LocalDateTime.now().plusDays(3)));

            assertThat(matching.getTitle()).isEqualTo("수정 후");
            assertThat(matching.getMaxPerson()).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("searchDetailMatching - 조건 검색")
    class SearchDetailMatchingTest {

        @Test
        @DisplayName("검색 조건을 그대로 리포지토리에 넘긴다")
        void search_delegatesToRepository() {
            MatchingCustomCond cond = new MatchingCustomCond("축구", null, null, null);
            given(matchingRepository.search(cond)).willReturn(List.of());

            var result = matchingService.searchDetailMatching(cond);

            assertThat(result).isEmpty();
            then(matchingRepository).should(times(1)).search(cond);
        }
    }
}

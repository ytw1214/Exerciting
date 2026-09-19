package com.exerciting.Exerciting.Domain.matching.matching.service;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingCreateResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingDetailResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingStatusResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingCustomCond;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.dto.MatchingParticipantDto;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.ParticipantStatus;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.matching.matching.event.MatchingCompletedEvent;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {
    private final MatchingRepository matchingRepository;
    private final UserRepository userRepository;
    private final MatchingChatRoomRepository matchingChatRoomRepository;
    private final MatchingParticipantRepository matchingParticipantRepository;
    private final GameRepository gameRepository;
    private final ApplicationEventPublisher eventPublisher;
    @Transactional
    public MatchingCreateResponseDto createMatching(MatchingRequestDto dto, Long hostId) {
        if (dto.getMeetTime().isBefore(LocalDateTime.now())) {
            throw new InvalidTimeException();
        }
        if (dto.getMaxPerson() < 2) {
            throw new InvalidInputException();
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new InvalidInputException();
        }
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new InvalidInputException());
        Game game = gameRepository.findById(dto.getGameId())
                .orElseThrow(InvalidInputException::new);
        Matching savedMatching = matchingRepository.save(dto.toEntity(host,game));
        MatchingChatRoom chatRoom = matchingChatRoomRepository.save(
                MatchingChatRoom.builder()
                        .matching(savedMatching)
                        .requester(host)
                        .build()
        );
        matchingParticipantRepository.save(
                MatchingParticipant.builder()
                        .user(host)
                        .matching(savedMatching)
                        .build()
        );
        savedMatching.refreshCapacityStatus(1);
        log.info("매칭 생성 완료 - matchingId: {}, host: {}", savedMatching.getId(), host.getUserId());
        return MatchingCreateResponseDto.of(savedMatching, chatRoom.getId(), 1);
    }
    public Matching findById(Long matchingId) {
        return matchingRepository.findById(matchingId)
                .orElseThrow(() -> new MatchingNotFoundException());
    }

    @Transactional
    public MatchingStatusResponseDto joinMatching(Long matchingId, Long userId) {
        Matching matching = matchingRepository.findByIdWithLock(matchingId)
                .orElseThrow(MatchingNotFoundException::new);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        long currentCount = matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED);
        // 상태와 인원을 한 번에 검증한다. 락으로 동시성을, 이 검증으로 도메인 불변식을 지킨다.
        matching.validateJoinable(currentCount);

        matchingParticipantRepository.findByMatchingAndUser(matching, user)
                .ifPresentOrElse(
                        MatchingParticipant::rejoin,   // 나갔던 사람은 기존 행을 되살린다
                        () -> matchingParticipantRepository.save(
                                MatchingParticipant.builder()
                                        .user(user)
                                        .matching(matching)
                                        .build()));

        matching.refreshCapacityStatus(currentCount + 1);
        log.info("매칭 참가 - matchingId: {}, userId: {}, 현재인원: {}/{}", matchingId, userId, currentCount + 1, matching.getMaxPerson());
        return MatchingStatusResponseDto.of(matching, currentCount + 1);
    }

    public Page<MatchingQueryResponseDto> getAllMatching(int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        return matchingRepository.findAll(pageable)
                .map(MatchingQueryResponseDto::from);
    }
    @Transactional
    public MatchingStatusResponseDto closeMatching(Long matchingId, Long currentUserId) {
        Matching matching = findMatchingByHost(matchingId, currentUserId);
        matching.close();
        log.info("매칭 마감 - matchingId - {}", matching.getId());
        return currentStatusOf(matching);
    }
    @Transactional
    public MatchingStatusResponseDto deleteMatching(Long matchingId, Long currentUserId) {
        Matching matching = findMatchingByHost(matchingId, currentUserId);
        // 기존: matchingRepository.delete(matching) — MatchingParticipant/MatchingChatRoom이
        // matching_id FK로 물려있어 cascade 미설정 상태에서는 참가자가 1명(호스트)만 있어도
        // 무결성 제약 위반 예외가 발생한다. 삭제 대신 상태 전환으로 이력을 보존한다.
        matching.cancel();
        log.info("매칭 취소(소프트 삭제) - matchingId: {}", matching.getId());
        return currentStatusOf(matching);
    }

    /**
     * meetTime이 지난 매칭을 완료 처리한다. MatchingScheduler가 주기적으로 호출.
     * 이 시점에 참가자 전원을 ATTENDED로 확정하고 MatchingCompletedEvent를 발행한다.
     * userReputation 도메인은 이 이벤트만 구독하면 되고, MatchingService는
     * 평판 계산 로직을 몰라도 된다(관심사 분리).
     */
    @Transactional
    public void completeMatching(Long matchingId) {
        Matching matching = matchingRepository.findByIdWithLock(matchingId)
                .orElseThrow(MatchingNotFoundException::new);
        if (matching.isTerminal()) {
            return; // 이미 완료/취소된 매칭은 중복 처리하지 않음
        }
        matching.complete();

        List<MatchingParticipant> activeParticipants =
                matchingParticipantRepository.findByMatchingAndStatus(matching, ParticipantStatus.JOINED);
        activeParticipants.forEach(MatchingParticipant::markAttended);

        List<Long> attendedUserIds = activeParticipants.stream()
                .map(p -> p.getUser().getId())
                .toList();

        log.info("매칭 완료 처리 - matchingId: {}, 참석자 {}명", matchingId, attendedUserIds.size());
        eventPublisher.publishEvent(new MatchingCompletedEvent(matching.getId(), matching.getUser().getId(), attendedUserIds));
    }
    @Transactional
    public MatchingStatusResponseDto updateMatching(Long matchingId, Long currentUserId, MatchingRequestDto changedDto) {
        Matching matching = findMatchingByHost(matchingId, currentUserId);
        long currentCount = matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED);
        matching.update(
                changedDto.getTitle(),
                changedDto.getDescription(),
                changedDto.getMaxPerson(),
                changedDto.getMeetTime(),
                currentCount);
        return MatchingStatusResponseDto.of(matching, currentCount);
    }
    @Transactional
    public MatchingStatusResponseDto reopenMatching(Long matchingId, Long currentUserId) {
        Matching matching = findMatchingByHost(matchingId, currentUserId);
        long currentCount = matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED);
        matching.reopen(currentCount);
        log.info("매칭 - {} 재오픈", matching.getId());
        return MatchingStatusResponseDto.of(matching, currentCount);
    }
    @Transactional(readOnly = true)
    // TODO: 목록 API와 동일하게 페이징 적용 필요 (현재 전체 반환)
    public List<MatchingQueryResponseDto> searchDetailMatching(MatchingCustomCond cond) {
        return matchingRepository.search(cond);
    }

    /**
     * 호스트 전용 변경(마감/재오픈/수정/취소)은 정원·상태를 바꾸므로 참가 요청과 경쟁한다.
     * 따라서 조회 시점부터 쓰기 락을 잡는다.
     */
    /** 응답에 담을 현재 인원을 세어 상태 DTO를 만든다. */
    private MatchingStatusResponseDto currentStatusOf(Matching matching) {
        long currentCount = matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED);
        return MatchingStatusResponseDto.of(matching, currentCount);
    }

    private Matching findMatchingByHost(Long matchingId, Long currentUserId) {
        Matching matching = matchingRepository.findByIdWithLock(matchingId)
                .orElseThrow(() -> new MatchingNotFoundException());
        if (!matching.isHost(currentUserId)) {
            log.warn("매칭 접근 권한 없음 - matchingId: {}, userId: {}", matchingId, currentUserId);
            throw new UnauthorizedUserException();
        }
        log.info("매칭 id {} 탐색 완료",matching.getId());
        return matching;
    }
    @Transactional
    public MatchingStatusResponseDto leaveMatching(Long matchingId, Long userId) {
        Matching matching = matchingRepository.findByIdWithLock(matchingId)
                .orElseThrow(() -> new MatchingNotFoundException());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        if (matching.isHost(userId)) {
            // 호스트는 나갈 수 없다. 매칭 자체를 취소해야 한다.
            throw new HostCannotLeaveException();
        }
        MatchingParticipant participant = matchingParticipantRepository
                .findByMatchingAndUserAndStatus(matching, user, ParticipantStatus.JOINED)
                .orElseThrow(NotParticipantException::new);
        // 기존: matchingParticipantRepository.delete(participant) — 이탈 이력이 사라져
        // "마감 직전 이탈" 같은 평판 신호를 만들 근거 데이터가 없어짐. 상태 전환으로 대체.
        participant.leave();
        long count = matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED);
        matching.refreshCapacityStatus(count);
        log.info("유저 {} - 매칭 {} 나감", userId, matchingId);
        return MatchingStatusResponseDto.of(matching, count);
    }
    @Transactional(readOnly = true)
    public MatchingDetailResponseDto getMatchingDetail(Long matchingId, Long userId) {
        Matching matching = findById(matchingId);
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        List<MatchingParticipantDto> participants = matchingParticipantRepository
                .findByMatchingId(matchingId)
                .stream()
                .map(MatchingParticipantDto::from)
                .toList();

        boolean isJoined = matchingParticipantRepository.existsByMatchingAndUserAndStatus(matching, user, ParticipantStatus.JOINED);

        return MatchingDetailResponseDto.of(matching, isJoined, participants);
    }
}
package com.exerciting.Exerciting.Domain.matching.matching.service;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingDetailResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingCustomCond;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.dto.MatchingParticipantDto;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Transactional
    public Long createMatching(MatchingRequestDto dto, Long hostId) {
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
        matchingChatRoomRepository.save(
                MatchingChatRoom.builder()
                        .matching(savedMatching)
                        .requester(host)
                        .build()
        );
        matchingParticipantRepository.save(
                MatchingParticipant.builder()
                        .user(host)
                        .matching(savedMatching)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        savedMatching.checkAndFull(1);
        log.info("매칭 생성 완료 - matchingId: {}, host: {}", savedMatching.getId(), host.getUserId());
        return savedMatching.getId();
    }
    public Matching findById(Long matchingId) {
        return matchingRepository.findById(matchingId)
                .orElseThrow(() -> new MatchingNotFoundException());
    }

    @Transactional
    public void joinMatching(Long matchingId, Long userId) {
        Matching matching = matchingRepository.findByIdWithLock(matchingId)
                .orElseThrow(()->new MatchingNotFoundException());
        if(!matching.isRecruiting()) {
            throw new InvalidInputException();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException());
        if(matchingParticipantRepository.existsByMatchingAndUserAndStatus(matching, user, ParticipantStatus.JOINED)) {
            throw new InvalidInputException();
        }
        matchingParticipantRepository.save(
                MatchingParticipant.builder()
                        .user(user)
                        .matching(matching)
                        .build()
        );
        long count = matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED);
        matching.checkAndFull(count);
        log.info("매칭 참가 - matchingId: {}, userId: {}, 현재인원: {}/{}", matchingId, userId, count, matching.getMaxPerson());
    }

    public Page<MatchingQueryResponseDto> getAllMatching(int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
        return matchingRepository.findAll(pageable)
                .map(MatchingQueryResponseDto::from);
    }
    @Transactional
    public void closeMatching(Long matchingId, Long currentUserId) {
        Matching matching = findMatchingByHost(matchingId, currentUserId);
        matching.close();
        log.info("매칭 마감 - matchingId - {}",matching.getId());
    }
    @Transactional
    public void deleteMatching(Long matchingId, Long currentUserId) {
        Matching matching = findMatchingByHost(matchingId, currentUserId);
        // 기존: matchingRepository.delete(matching) — MatchingParticipant/MatchingChatRoom이
        // matching_id FK로 물려있어 cascade 미설정 상태에서는 참가자가 1명(호스트)만 있어도
        // 무결성 제약 위반 예외가 발생한다. 삭제 대신 상태 전환으로 이력을 보존한다.
        matching.cancel();
        log.info("매칭 취소(소프트 삭제) - matchingId: {}", matching.getId());
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
    public void updateMatching(Long matchingId, Long currentUserId, MatchingRequestDto changedDto) {
        Matching matching = findMatchingByHost(matchingId, currentUserId);
        matching.update(
                changedDto.getTitle(),
                changedDto.getDescription(),
                changedDto.getMaxPerson(),
                changedDto.getMeetTime());
    }
    @Transactional
    public void reopenMatching(Long matchingId, Long currentUserId) {
        Matching matching = findMatchingByHost(matchingId,currentUserId);
        matching.reopen();
        log.info("매칭 - {} 재오픈",matching.getId());
    }
    @Transactional(readOnly = true)
    public List<MatchingQueryResponseDto> searchDetailMatching(MatchingCustomCond cond, Long currentUserId) {
        if (!userRepository.existsById(currentUserId)) {
            throw new UnauthorizedUserException();
        }
        return matchingRepository.search(cond);

    }

    private Matching findMatchingByHost(Long matchingId, Long currentUserId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new MatchingNotFoundException());
        if (!matching.getUser().getId().equals(currentUserId)) {
            log.warn("매칭 접근 권한 없음 - matchingId: {}, userId: {}", matchingId, currentUserId);
            throw new UnauthorizedUserException();
        }
        log.info("매칭 id {} 탐색 완료",matching.getId());
        return matching;
    }
    @Transactional
    public void leaveMatching(Long matchingId, Long userId) {
        Matching matching = matchingRepository.findByIdWithLock(matchingId)
                .orElseThrow(() -> new MatchingNotFoundException());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());
        if(matching.getUser().getId().equals(userId)) {
            throw new UnauthorizedUserException();
        }
        MatchingParticipant participant = matchingParticipantRepository
                .findByMatchingAndUserAndStatus(matching, user, ParticipantStatus.JOINED)
                .orElseThrow(InvalidInputException::new);
        // 기존: matchingParticipantRepository.delete(participant) — 이탈 이력이 사라져
        // "마감 직전 이탈" 같은 평판 신호를 만들 근거 데이터가 없어짐. 상태 전환으로 대체.
        participant.leave();
        long count = matchingParticipantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED);
        matching.checkAndReopen(count);
        log.info("유저 {} - 매칭 {} 나감", userId, matchingId);
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
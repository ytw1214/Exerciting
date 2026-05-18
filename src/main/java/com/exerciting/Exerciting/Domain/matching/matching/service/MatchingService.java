package com.exerciting.Exerciting.Domain.matching.matching.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingCustomCond;
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

        Matching savedMatching = matchingRepository.save(dto.toEntity(host));
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
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(()->new MatchingNotFoundException());
        if(!matching.isRecruiting()) {
            throw new InvalidInputException();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException());
        if(matchingParticipantRepository.existsByMatchingAndUser(matching,user)) {
            throw new InvalidInputException();
        }
        matchingParticipantRepository.save(
                MatchingParticipant.builder()
                        .user(user)
                        .matching(matching)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        long count = matchingParticipantRepository.countByMatching(matching);
        matching.checkAndFull(count);
        log.info("매칭 참가 - matchingId: {}, userId: {}, 현재인원: {}/{}", matchingId, userId, count, matching.getMaxPerson());
    }

    public List<Matching> getAllMatching() {
        return matchingRepository.findAll();
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
        matchingRepository.delete(matching);
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
}

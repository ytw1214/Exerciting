package com.exerciting.Exerciting.Domain.matching.matching.service;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.service.MatchingChatService;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.service.MatchingChatRoomService;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingCustomCond;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Exception.*;
import jakarta.persistence.EntityNotFoundException;
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
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
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

        Matching matching = dto.toEntity(host);
        Matching savedMatching = matchingRepository.save(matching);
        //matchingChatService.createChatRoom(savedMatching, host);
        return savedMatching.getId();
    }
    public Matching findById(Long matchingId) {
        return matchingRepository.findById(matchingId)
                .orElseThrow(() -> new MatchingNotFoundException());
    }
    /*
    public List<MatchingRequestDto> getMatchingByTeam(String teamName) {
        List<Matching> list = matchingRepository.findByTeamName(teamName);
        List<MatchingRequestDto> requestList = new ArrayList<>();
        for(Matching matching : list) {
            MatchingRequestDto dto = MatchingRequestDto.fromEntity(matching);
            requestList.add(dto);
        }
        return requestList;
    }

     */
    public List<Matching> getAllMatching() {
        return matchingRepository.findAll();
    }

    public void deleteMatching(Long matchingId, Long currentUserId) {
        Matching matching = searchMatching(matchingId, currentUserId);
        matchingRepository.delete(matching);
    }
    @Transactional
    public void updateMatching(Long matchingId, Long currentUserId, MatchingRequestDto changedDto) {
        Matching matching = searchMatching(matchingId, currentUserId);
        matching.update(
                changedDto.getTitle(),
                changedDto.getDescription(),
                changedDto.getMaxPerson(),
                changedDto.getMeetTime());
    }
    private Matching searchMatching(Long matchingId, Long currentUserId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new EntityNotFoundException("해당 모임이 존재하지 않습니다."));
        verify(matching, currentUserId);
        log.info("매칭 id {} 탐색 완료",matching.getId());
        return matching;
    }
    private void verify(Matching matching, Long currentUserId) {
        if(!matching.getUser().getId().equals(currentUserId)) {
            log.info("매칭 접근 오류");
            throw new UnauthorizedUserException();
        }
    }
    @Transactional(readOnly = true)
    public List<MatchingQueryResponseDto> searchDetailMatching(MatchingCustomCond cond, Long currentUserId) {
        if (!userRepository.existsById(currentUserId)) {
            throw new UnauthorizedUserException();
        }
        return matchingRepository.search(cond);

    }

}

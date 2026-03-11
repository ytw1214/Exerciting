package com.exerciting.Exerciting.Domain.matching.service;

import com.exerciting.Exerciting.Domain.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.repository.MatchingCustomCond;
import com.exerciting.Exerciting.Domain.matching.repository.MatchingRepositoryCustomImpl;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Domain.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.InvalidTimeException;
import com.exerciting.Exerciting.Exception.UnauthorizedUserException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MatchingService {
    private final MatchingRepository matchingRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    public MatchingService(MatchingRepository matchingRepository, GameRepository gameRepository, UserRepository userRepository) {
        this.matchingRepository = matchingRepository;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    public Long createMatching(MatchingRequestDto dto, Long hostId) {
        if (dto.getMeetTime().isBefore(LocalDateTime.now())) {
            throw new InvalidTimeException("시간 오류 ~");
        }
        if (dto.getMaxPerson() < 2) {
            throw new InvalidInputException("인원 부족~");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new InvalidInputException("매칭 이름은 필수 입력이며, 공백으로만 이루어질 수 없습니다.");
        }
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new InvalidInputException("존재하지 않는 사용자입니다."));

        Matching matching = dto.toEntity(host);
        Matching savedMatching = matchingRepository.save(matching);
        return savedMatching.getId();
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
            throw new UnauthorizedUserException("잘못된 접근입니다.");
        }
    }
    @Transactional(readOnly = true)
    public List<MatchingQueryResponseDto> searchDetailMatching(MatchingCustomCond cond, Long currentUserId) {
        if (!userRepository.existsById(currentUserId)) {
            throw new UnauthorizedUserException("인증된 사용자만 조회가 가능합니다.");
        }
        return matchingRepository.search(cond);

    }
}

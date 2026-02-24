package com.exerciting.Exerciting.Domain.matching.service;

import com.exerciting.Exerciting.Domain.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.team.entity.Team;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class MatchingService {
    private final MatchingRepository matchingRepository;
    private final GameRepository gameRepository;

    public MatchingService(MatchingRepository matchingRepository, GameRepository gameRepository) {
        this.matchingRepository = matchingRepository;
        this.gameRepository = gameRepository;
    }

    /*
    public Long createMatching(MatchingRequestDto dto, Long hostId) {
        // 1. 여기서부터 시작이다.
        if (dto.getMeetTime().isBefore(LocalDateTime.now())) {
            throw new InvalidTimeException("시간 오류 ~");
        }
        if (dto.getMaxPerson() < 2) {
            throw new InvalidInputException("인원 부족~");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new InvalidInputException("매칭 이름은 필수 입력이며, 공백으로만 이루어질 수 없습니다.");
        }

        Matching matching = dto.toEntity(hostId);
        Matching savedMatching = matchingRepository.save(matching);
        return matching.getId();
    }

     */
    public List<MatchingRequestDto> getMatchingByTeam(Team team) {
        return matchingRepository.findByTeam(team);
    }
    public List<Matching> getAllMatching() {
        return matchingRepository.findAll();
    }



}

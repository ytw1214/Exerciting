package com.exerciting.Exerciting.Domain.matching.service;

import com.exerciting.Exerciting.Domain.User.entity.User;
import com.exerciting.Exerciting.Domain.User.repository.UserRepository;
import com.exerciting.Exerciting.Domain.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.team.entity.Team;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.InvalidTimeException;
import com.exerciting.Exerciting.Exception.UnauthorizedUserException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public List<MatchingRequestDto> getMatchingByTeam(String teamName) {
        List<Matching> list = matchingRepository.findByTeamName(teamName);
        List<MatchingRequestDto> requestList = new ArrayList<>();
        for(Matching matching : list) {
            MatchingRequestDto dto = MatchingRequestDto.fromEntity(matching);
            requestList.add(dto);
        }
        return requestList;
    }
    public List<Matching> getAllMatching() {
        return matchingRepository.findAll();
    }

    public void deleteMatching() {
        Matching matching = matchingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 모임이 존재하지 않습니다."));


    }
    private Matching searchMatching(Long matchingId, Long currentUserId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new EntityNotFoundException("해당 모임이 존재하지 않습니다."));

        if(!matching.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedUserException("잘못된 접근입니다.");
        }
        return matching;

    }

}

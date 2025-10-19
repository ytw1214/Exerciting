package com.exerciting.Exerciting.Service;

import com.exerciting.Exerciting.Entity.Matching;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Exception.InvalidTimeException;
import com.exerciting.Exerciting.Repository.MatchingRepository;
import com.exerciting.Exerciting.dto.GetMatchingDto;
import com.exerciting.Exerciting.dto.MatchingRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchService {
    private final MatchingRepository matchingRepository;

    @Autowired
    public MatchService(MatchingRepository matchingRepository) {
        this.matchingRepository = matchingRepository;

    }
    public Long createMatching(MatchingRequestDto dto, Long hostId) {
        // 1. 여기서부터 시작이다.
        if(dto.getMatchTime().isBefore(LocalDateTime.now())) {
            throw new InvalidTimeException("시간 오류 ~");
        }
        if(dto.getMaxMember() < 2) {
            throw new InvalidInputException("인원 부족~");
        }
        if (dto.getMatchName() == null || dto.getMatchName().isBlank()) {
            throw new InvalidInputException("매칭 이름은 필수 입력이며, 공백으로만 이루어질 수 없습니다.");
        }
        Matching matching = dto.toEntity(hostId);
        Matching savedMatching = matchingRepository.save(matching);
        return matching.getId();
    }
    public List<Matching> getAllMatching() {
        return matchingRepository.findAll();
    }
}

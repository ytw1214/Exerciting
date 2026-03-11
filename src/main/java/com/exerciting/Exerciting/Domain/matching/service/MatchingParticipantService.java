package com.exerciting.Exerciting.Domain.matching.service;

import com.exerciting.Exerciting.Domain.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.matching.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchingParticipantService {
    private final MatchingParticipantRepository matchingParticipantRepository;
    private final MatchingRepository matchingRepository;
    public List<MatchingParticipant> getUserByMatching(Long matchingId) {
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new InvalidInputException("잘못된 매칭입니다."));
        return matchingParticipantRepository.findByMatchingId(matching.getId());
    }
}

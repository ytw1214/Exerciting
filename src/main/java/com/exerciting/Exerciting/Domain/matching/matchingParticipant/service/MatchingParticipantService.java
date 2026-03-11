package com.exerciting.Exerciting.Domain.matching.matchingParticipant.service;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.MatchingParticipant;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

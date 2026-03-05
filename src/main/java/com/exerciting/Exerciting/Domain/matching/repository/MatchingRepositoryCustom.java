package com.exerciting.Exerciting.Domain.matching.repository;

import com.exerciting.Exerciting.Domain.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.entity.QMatching;

import java.util.List;

public interface MatchingRepositoryCustom {
    List<MatchingQueryResponseDto> search(MatchingCustomCond matchingCustomCond);
}

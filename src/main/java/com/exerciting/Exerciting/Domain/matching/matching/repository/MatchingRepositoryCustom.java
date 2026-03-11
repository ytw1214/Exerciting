package com.exerciting.Exerciting.Domain.matching.matching.repository;

import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;

import java.util.List;

public interface MatchingRepositoryCustom {
    List<MatchingQueryResponseDto> search(MatchingCustomCond matchingCustomCond);
}

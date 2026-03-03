package com.exerciting.Exerciting.Domain.matching.repository;

import com.exerciting.Exerciting.Domain.matching.entity.Matching;

import java.util.List;

public interface MatchingRepositoryCustom {
    List<Matching> search(MatchingCustomCond matchingCustomCond);
}

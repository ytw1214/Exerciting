package com.exerciting.Exerciting.Domain.matching.repository;

import com.exerciting.Exerciting.Domain.matching.entity.QMatching;

import java.util.List;

public interface MatchingRepositoryCustom {
    List<QMatching> search(MatchingCustomCond matchingCustomCond);
}

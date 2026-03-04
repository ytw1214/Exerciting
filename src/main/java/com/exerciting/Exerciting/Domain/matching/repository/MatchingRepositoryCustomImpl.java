package com.exerciting.Exerciting.Domain.matching.repository;
import com.exerciting.Exerciting.Domain.matching.entity.QMatching;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.exerciting.Exerciting.Domain.game.entity.QGame.game;
import static com.exerciting.Exerciting.Domain.matching.entity.QMatching.matching;
@RequiredArgsConstructor
public class MatchingRepositoryCustomImpl implements MatchingRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    public List<QMatching> search(MatchingCustomCond cond) {
        return null;
    }
    public BooleanExpression titleContains(String title) {
        return title == null ? null : matching.title.containsIgnoreCase(title);
    }
    public BooleanExpression descriptionContains(String description) {
        return description == null ? null : matching.description.containsIgnoreCase(description);
    }
    public BooleanExpression meetTimeContains(LocalDateTime meetTime) {
        if(meetTime == null) {
            return matching.meetTime.goe(LocalDateTime.now());
        }
        return matching.meetTime.goe(meetTime);
    }

    public BooleanExpression teamNameContains(String teamName) {
        if(teamName == null) {
            return null;
        }
        return game.homeTeam.name_english.containsIgnoreCase(teamName)
                .or(game.awayTeam.name_english.containsIgnoreCase(teamName));
    }
}

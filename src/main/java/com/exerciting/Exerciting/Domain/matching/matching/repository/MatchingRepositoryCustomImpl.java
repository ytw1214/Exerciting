package com.exerciting.Exerciting.Domain.matching.matching.repository;
import com.exerciting.Exerciting.Domain.game.entity.QGame;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;

import com.exerciting.Exerciting.Domain.matching.matching.entity.QMatching;
import com.exerciting.Exerciting.Domain.stadium.entity.QStadium;
import com.exerciting.Exerciting.Domain.team.entity.QTeam;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class MatchingRepositoryCustomImpl implements MatchingRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QMatching matching = QMatching.matching;
    private final QTeam homeTeam = new QTeam("homeTeam");
    private final QTeam awayTeam = new QTeam("awayTeam");
    private final QStadium stadium = QStadium.stadium;
    private final QGame game = QGame.game;
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
        return homeTeam.name_english.containsIgnoreCase(teamName)
                .or(awayTeam.name_english.containsIgnoreCase(teamName));
    }
    public List<MatchingQueryResponseDto> search(MatchingCustomCond cond) {
        return queryFactory
                .select(Projections.constructor(MatchingQueryResponseDto.class,
                        matching.id,
                        matching.title,
                        matching.currentPerson,
                        matching.maxPerson,
                        matching.meetTime,
                        game.homeTeam.name_english,
                        game.awayTeam.name_english,
                        stadium.name,
                        game.sportType.stringValue()))
                .from(matching)
                .leftJoin(matching.game,game)
                .leftJoin(game.homeTeam, homeTeam)
                .leftJoin(game.awayTeam, awayTeam)
                .leftJoin(game.stadium, stadium)
                .where(titleContains(cond.title()),
                        descriptionContains(cond.description()),
                        meetTimeContains(cond.meetTime()),
                        teamNameContains(cond.teamName())
                )
                .fetch();
    }
}

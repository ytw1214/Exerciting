package com.exerciting.Exerciting.Domain.game.repository;

import com.exerciting.Exerciting.Domain.game.dto.GameQueryResponseDto;
import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.entity.QTeam;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.exerciting.Exerciting.Domain.game.entity.QGame.game;
import static com.exerciting.Exerciting.Domain.stadium.entity.QStadium.stadium;

@RequiredArgsConstructor
public class GameRepositoryCustomImpl implements GameRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QTeam homeTeam = new QTeam("homeTeam");
    private final QTeam awayTeam = new QTeam("awayTeam");

    public List<GameQueryResponseDto> search(GameCustomCond gameCustomCond, LocalDateTime start, LocalDateTime end) {
        return queryFactory.select(
                        Projections.constructor(GameQueryResponseDto.class,
                                game.id,
                                game.gameStartTime,
                                game.homeTeam.name,
                                game.awayTeam.name,
                                game.sportType.stringValue(),
                                game.gameStatus.stringValue(),
                                stadium.name
                        ))
                .from(game)
                .leftJoin(game.stadium, stadium)
                .leftJoin(game.homeTeam, homeTeam)
                .leftJoin(game.awayTeam, awayTeam)
                .where(
                        eqSportType(gameCustomCond),
                        eqGameStatus(gameCustomCond),
                        eqDateTime(gameCustomCond.startTime()),
                        eqTeamName(gameCustomCond)
                )
                .orderBy(game.gameStatus.asc(), game.gameStartTime.asc())
                .fetch();

    }
    private BooleanExpression eqDateTime(LocalDate localDate) {
        if(localDate == null) {
            return null;
        }
        LocalDateTime startDate = localDate.atStartOfDay();
        LocalDateTime endDate = localDate.atTime(LocalTime.MAX);

        return game.gameStartTime.between(startDate, endDate);
    }
    private BooleanExpression eqSportType(GameCustomCond cond) {
        if (cond.sportType() == null) {
            return null;
        }
        return game.sportType.eq(cond.sportType());
    }

    private BooleanExpression eqGameStatus(GameCustomCond cond) {
        if (cond.gameStatus() == null) {
            return null;
        }
        return game.gameStatus.eq(cond.gameStatus());
    }
    private BooleanExpression eqTeamName(GameCustomCond cond) {
        if(cond.teamName() == null) {
            return null;
        }
        if(cond.containsAwayTeam()) {
            return homeTeam.name.eq(cond.teamName())
                    .or(awayTeam.name.eq(cond.teamName()));
        }
        return homeTeam.name.eq(cond.teamName());
    }
}

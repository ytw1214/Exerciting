package com.exerciting.Exerciting.Domain.game.repository;

import com.exerciting.Exerciting.Domain.game.dto.GameQueryResponseDto;
import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.team.entity.QTeam;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.exerciting.Exerciting.Domain.game.entity.QGame.game;
import static com.exerciting.Exerciting.Domain.stadium.entity.QStadium.stadium;

@RequiredArgsConstructor
public class GameRepositoryCustomImpl implements GameRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private final QTeam homeTeam = new QTeam("homeTeam");
    private final QTeam awayTeam = new QTeam("awayTeam");

    public List<GameQueryResponseDto> search(GameCustomCond gameCustomCond) {
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
                        eqSportType(gameCustomCond.sportType()),
                        eqGameStatus(gameCustomCond.gameStatus())
                )
                .orderBy(game.gameStatus.asc(), game.gameStartTime.asc())
                .fetch();

    }

    private BooleanExpression eqSportType(SportType sportType) {
        // 값이 없으면 null을 반환 -> Querydsl where절은 null을 만나면 자동으로 무시함! (핵심)
        if (sportType == null) {
            return null;
        }
        return game.sportType.eq(sportType);
    }

    private BooleanExpression eqGameStatus(GameStatus gameStatus) {
        // 값이 없으면 null을 반환 -> Querydsl where절은 null을 만나면 자동으로 무시함! (핵심)
        if (gameStatus == null) {
            return null;
        }
        return game.gameStatus.eq(gameStatus);
    }
}

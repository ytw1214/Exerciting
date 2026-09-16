package com.exerciting.Exerciting.Game.Repository;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.game.repository.GameCustomCond;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.stadium.entity.Stadium;
import com.exerciting.Exerciting.Domain.team.entity.Team;
import com.exerciting.Exerciting.ExercitingApplication;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = ExercitingApplication.class)
@ActiveProfiles("test")
@Import(GameRepositoryTest.TestConfig.class)
@DisplayName("GameRepository 통합 테스트")
class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Stadium stadium;
    private Team homeTeam;
    private Team awayTeam;

    @BeforeEach
    void setUp() {
        // Stadium 저장
        stadium = Stadium.builder()
                .name("잠실야구장")
                .latitude(37.5122)
                .longitude(127.0719)
                .address("서울 송파구")
                .shortName("잠실")
                .build();
        entityManager.persist(stadium);

        // 홈팀 저장
        homeTeam = Team.builder()
                .name("LG 트윈스")
                .stadium(new ArrayList<>(List.of(stadium)))
                .sportType(SportType.BASEBALL)
                .imgUrl("https://example.com/lg.png")
                .build();
        entityManager.persist(homeTeam);

        // 어웨이팀 저장
        awayTeam = Team.builder()
                .name("KT 위즈")
                .stadium(new ArrayList<>())
                .sportType(SportType.BASEBALL)
                .imgUrl("https://example.com/kt.png")
                .build();
        entityManager.persist(awayTeam);

        entityManager.flush();
    }

    @Test
    @DisplayName("팀 이름으로 경기 조회 시 홈/어웨이 모두 포함해서 반환")
    void 팀이름으로_경기조회() {
        Game game = Game.builder()
                .sportType(SportType.BASEBALL)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .gameStartTime(LocalDateTime.of(2024, 5, 1, 14, 0))
                .gameStatus(GameStatus.BEFORE)
                .build();
        gameRepository.save(game);
        entityManager.flush();
        entityManager.clear();


        List<Game> results = gameRepository.findByTeamName("LG 트윈스");


        assertThat(results).hasSize(1);
        assertThat(results.get(0).getHomeTeam().getName()).isEqualTo("LG 트윈스");

        System.out.println("[테스트1 통과] 팀 이름 조회: " + results.get(0).getHomeTeam().getName());
    }

    @Test
    @DisplayName("경기 상태 BEFORE인 경기만 조회")
    void 경기상태_BEFORE_조회() {
        // given: BEFORE 1개, FINISHED 1개
        Game beforeGame = Game.builder()
                .sportType(SportType.BASEBALL)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .gameStartTime(LocalDateTime.now().plusDays(1))
                .gameStatus(GameStatus.BEFORE)
                .build();

        Game finishedGame = Game.builder()
                .sportType(SportType.BASEBALL)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .gameStartTime(LocalDateTime.now().minusDays(1))
                .gameStatus(GameStatus.FINISHED)
                .build();

        gameRepository.saveAll(List.of(beforeGame, finishedGame));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Game> results = gameRepository.findByGameStatus(GameStatus.BEFORE);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getGameStatus()).isEqualTo(GameStatus.BEFORE);

        System.out.println("[테스트2 통과] BEFORE 상태 경기 수: " + results.size());
    }

    @Test
    @DisplayName("QueryDSL sportType 조건 — BASEBALL만 조회")
    void QueryDSL_sportType_필터링() {

        Game baseballGame = Game.builder()
                .sportType(SportType.BASEBALL)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .gameStartTime(LocalDateTime.of(2024, 5, 15, 14, 0))
                .gameStatus(GameStatus.BEFORE)
                .build();
        gameRepository.save(baseballGame);
        entityManager.flush();
        entityManager.clear();


        GameCustomCond cond = GameCustomCond.of(null, SportType.BASEBALL, LocalDate.of(2024, 5, 1), null, false);
        LocalDateTime start = LocalDateTime.of(2024, 5, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 5, 31, 23, 59, 59);

        var results = gameRepository.search(cond, start, end);


        assertThat(results).hasSize(1);
        assertThat(results.get(0).sportType()).isEqualTo("BASEBALL");

        System.out.println("[테스트3 통과] QueryDSL BASEBALL 필터: " + results.size() + "건");
    }
    @Test
    @DisplayName("같은 홈팀/어웨이팀/시간의 경기가 이미 존재하면 true 반환")
    void 중복경기_존재여부_확인() {
        // given
        LocalDateTime gameTime = LocalDateTime.of(2024, 6, 1, 14, 0);
        Game game = Game.builder()
                .sportType(SportType.BASEBALL)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .gameStartTime(gameTime)
                .gameStatus(GameStatus.BEFORE)
                .build();
        gameRepository.save(game);
        entityManager.flush();
        entityManager.clear();


        boolean exists = gameRepository.existsByHomeTeamAndAwayTeamAndGameStartTime(
                homeTeam, awayTeam, gameTime
        );


        assertThat(exists).isTrue();

        System.out.println("[테스트4 통과] 중복 경기 감지: " + exists);
    }

    @Test
    @DisplayName("다른 시간의 경기는 중복이 아님")
    void 다른시간_경기_중복아님() {
        // given
        LocalDateTime gameTime = LocalDateTime.of(2024, 6, 1, 14, 0);
        Game game = Game.builder()
                .sportType(SportType.BASEBALL)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .gameStartTime(gameTime)
                .gameStatus(GameStatus.BEFORE)
                .build();
        gameRepository.save(game);
        entityManager.flush();
        entityManager.clear();


        boolean exists = gameRepository.existsByHomeTeamAndAwayTeamAndGameStartTime(
                homeTeam, awayTeam, gameTime.plusDays(1)
        );

        assertThat(exists).isFalse();

        System.out.println("[테스트5 통과] 다른 시간 경기 중복 아님: " + exists);
    }

    @Configuration
    static class TestConfig {
        @Bean
        public JPAQueryFactory jpaQueryFactory(EntityManager em) {
            return new JPAQueryFactory(em);
        }
    }
}
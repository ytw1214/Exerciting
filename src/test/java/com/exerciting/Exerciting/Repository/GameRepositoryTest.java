/*
package com.exerciting.Exerciting.Repository;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.stadium.entity.Stadium;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // JPA 테스트를 위한 설정
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class GameRepositoryTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("홈 팀 이름으로 경기 목록을 조회한다")
    void findByHomeTeamTest() {
        // given (준비)
        Stadium stadium = new Stadium("허겸", 100.00, 200.00, "부안");
        entityManager.persist(stadium);
        Game game = Game.builder()
                .sportType("soccer")
                .homeTeam("Lions")
                .awayTeam("Tigers")
                .stadium(stadium)
                .gameStartTime(LocalDateTime.now())
                .build();
        gameRepository.save(game);
        /*
        entityManager.persist(game); // DB에 저장
        entityManager.flush();


        // when (실행)
        List<Game> results = gameRepository.findByHomeTeam("Lions");

        // then (검증)
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getHomeTeam()).isEqualTo("Lions");
    }

}


         */
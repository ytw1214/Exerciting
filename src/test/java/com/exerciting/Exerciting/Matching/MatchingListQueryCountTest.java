package com.exerciting.Exerciting.Matching;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingQueryResponseDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.stadium.entity.Stadium;
import com.exerciting.Exerciting.Domain.stadium.repository.StadiumRepository;
import com.exerciting.Exerciting.Domain.team.entity.Team;
import com.exerciting.Exerciting.Domain.team.repository.TeamRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * README §2 "N+1 개선 전/후 쿼리 수"를 숫자로 남기는 테스트.
 *
 * 서로 다른 경기·팀·경기장을 가진 매칭 10건의 목록을 한 페이지로 조회하고, 실행된 SQL 수를 센다.
 * 개선 전 수치가 궁금하면 MatchingRepository.findAllByStatusIn의 @EntityGraph를 잠깐 주석 처리하고 실행한다
 * (로그에 쿼리 수가 먼저 찍힌 뒤 단언이 실패한다).
 *
 * 테스트 트랜잭션으로 영속성 컨텍스트를 열어 두는 것은 운영의 OSIV와 같은 조건을 만들기 위해서다.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:querycount;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.jpa.show-sql=false"
})
class MatchingListQueryCountTest {

    private static final int MATCHING_COUNT = 10;

    @Autowired
    private MatchingService matchingService;
    @Autowired
    private MatchingRepository matchingRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private StadiumRepository stadiumRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    @DisplayName("매칭 10건 목록 조회는 매칭 수와 무관하게 쿼리 2번(목록 + count)으로 끝난다")
    void matchingList_hasNoNPlusOne() {
        User host = userRepository.save(User.builder()
                .userId("qc_host").pw("pw").nickname("qc_host").name("호스트").email("qc_host@test.com").build());

        for (int i = 0; i < MATCHING_COUNT; i++) {
            Stadium stadium = stadiumRepository.save(Stadium.builder().name("경기장" + i).shortName("S" + i).build());
            Team home = teamRepository.save(Team.builder().name("홈팀" + i).sportType(SportType.BASEBALL).stadium(new ArrayList<>()).build());
            Team away = teamRepository.save(Team.builder().name("원정팀" + i).sportType(SportType.BASEBALL).stadium(new ArrayList<>()).build());
            Game game = gameRepository.save(Game.builder()
                    .sportType(SportType.BASEBALL).homeTeam(home).awayTeam(away).stadium(stadium)
                    .gameStartTime(LocalDateTime.now().plusDays(1)).gameStatus(GameStatus.BEFORE).build());
            matchingRepository.save(Matching.builder()
                    .title("매칭" + i).description("쿼리 수 측정").maxPerson(4)
                    .user(host).game(game).meetTime(LocalDateTime.now().plusDays(1)).build());
        }
        // 1차 캐시에 남은 엔티티 때문에 쿼리가 덜 세어지지 않도록 비운다
        entityManager.flush();
        entityManager.clear();

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        Page<MatchingQueryResponseDto> page = matchingService.getAllMatching(0, MATCHING_COUNT);

        long queryCount = statistics.getPrepareStatementCount();
        log.info("[N+1 측정] 매칭 {}건 목록 조회에 실행된 SQL: {}회", page.getContent().size(), queryCount);

        assertThat(page.getContent()).hasSize(MATCHING_COUNT);
        assertThat(page.getContent()).allSatisfy(dto -> {
            assertThat(dto.homeTeamEnglish()).isNull();   // 테스트 팀에는 shortName이 없다
            assertThat(dto.stadiumName()).startsWith("경기장");
        });
        assertThat(queryCount).isLessThanOrEqualTo(2);
    }
}

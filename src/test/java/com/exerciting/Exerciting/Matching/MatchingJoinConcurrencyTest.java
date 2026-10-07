package com.exerciting.Exerciting.Matching;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.game.entity.GameStatus;
import com.exerciting.Exerciting.Domain.game.repository.GameRepository;
import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.matching.matching.dto.MatchingRequestDto;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.entity.MatchingStatus;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.matching.matching.service.MatchingService;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity.ParticipantStatus;
import com.exerciting.Exerciting.Domain.matching.matchingParticipant.repository.MatchingParticipantRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Exception.MatchingFullException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * README §1 "동시에 참가 요청이 몰려도 정원을 넘지 않는다"를 증명하는 테스트.
 *
 * 스레드 100개가 출발 신호(CountDownLatch)를 기다렸다가 같은 매칭에 동시에 참가를 요청한다.
 * 비관적 락(SELECT ... FOR UPDATE)이 없다면 여러 스레드가 같은 인원 수를 읽고 동시에 참가해 정원을 넘는다.
 *
 * DB는 H2(MySQL 모드)다. 락 정합성은 여기서 회귀 테스트로 지키고,
 * 실제 MySQL에서의 수치는 k6/join-concurrency.js로 따로 잰다.
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // 다른 테스트 컨텍스트와 섞이지 않도록 전용 인메모리 DB, 락 대기 10초
        "spring.datasource.url=jdbc:h2:mem:concurrency;MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000"
})
class MatchingJoinConcurrencyTest {

    private static final int CAPACITY = 5;      // 호스트 포함 정원
    private static final int REQUESTS = 100;    // 동시에 참가를 누르는 사람 수

    @Autowired
    private MatchingService matchingService;
    @Autowired
    private MatchingRepository matchingRepository;
    @Autowired
    private MatchingParticipantRepository participantRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;

    @Test
    @DisplayName("정원 5명 매칭에 100명이 동시에 참가해도 호스트 포함 정확히 5명만 들어간다")
    void concurrentJoin_neverExceedsCapacity() throws Exception {
        // given
        User host = userRepository.save(newUser("host"));
        Game game = gameRepository.save(Game.builder()
                .sportType(SportType.BASEBALL)
                .gameStartTime(LocalDateTime.now().plusDays(2))
                .gameStatus(GameStatus.BEFORE)
                .build());
        Long matchingId = matchingService.createMatching(
                new MatchingRequestDto("동시성 테스트", "정원 보장 검증", CAPACITY, LocalDateTime.now().plusDays(1), game.getId()),
                host.getId()).matchingId();
        List<Long> guestIds = IntStream.range(0, REQUESTS)
                .mapToObj(i -> userRepository.save(newUser("g" + i)).getId())
                .toList();

        ExecutorService executor = Executors.newFixedThreadPool(REQUESTS);
        CountDownLatch ready = new CountDownLatch(REQUESTS);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejectedFull = new AtomicInteger();
        Queue<Throwable> unexpected = new ConcurrentLinkedQueue<>();

        for (Long guestId : guestIds) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    matchingService.joinMatching(matchingId, guestId);
                    success.incrementAndGet();
                } catch (MatchingFullException e) {
                    rejectedFull.incrementAndGet();
                } catch (Throwable t) {
                    unexpected.add(t);
                }
            });
        }

        // when: 100개 스레드가 모두 준비되면 동시에 출발
        ready.await(10, TimeUnit.SECONDS);
        long startedAt = System.nanoTime();
        start.countDown();
        executor.shutdown();
        boolean finished = executor.awaitTermination(60, TimeUnit.SECONDS);
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        // then
        Matching matching = matchingRepository.findById(matchingId).orElseThrow();
        long joined = participantRepository.countByMatchingAndStatus(matching, ParticipantStatus.JOINED);
        log.info("[동시 참가] 정원 {}명, 요청 {}건 → 성공 {}건 / 정원 초과 거절 {}건 / 예상 밖 오류 {}건, 최종 참가자 {}명, 소요 {}ms",
                CAPACITY, REQUESTS, success.get(), rejectedFull.get(), unexpected.size(), joined, elapsedMs);

        assertThat(finished).isTrue();
        assertThat(unexpected).isEmpty();
        assertThat(success.get()).isEqualTo(CAPACITY - 1);             // 호스트를 뺀 4명
        assertThat(rejectedFull.get()).isEqualTo(REQUESTS - (CAPACITY - 1));
        assertThat(joined).isEqualTo(CAPACITY);                        // 초과 참가 0건
        assertThat(matching.getStatus()).isEqualTo(MatchingStatus.FULL);
    }

    private User newUser(String key) {
        return User.builder()
                .userId("cc_" + key)
                .pw("pw")
                .nickname("n_" + key)
                .name("동시성")
                .email(key + "@concurrency.test")
                .build();
    }
}

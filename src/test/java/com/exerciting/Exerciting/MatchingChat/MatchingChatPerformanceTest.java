package com.exerciting.Exerciting.MatchingChat;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository.MatchingChatRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import com.exerciting.Exerciting.Support.QueryTimer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MySQL(JPA) 채팅 메시지 삽입/조회 성능 측정.
 * 100개 방 x 방당 1,000건 = 총 10만 건. 비교 대상: MatchingChatMongoPerformanceTest
 *
 * perf 프로필은 별도 DB(exerciting_perf)를 쓰고 실행마다 테이블을 새로 만들기 때문에
 * 개발용 로컬 데이터는 건드리지 않는다.
 */
@SpringBootTest
@ActiveProfiles({"local", "perf"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class MatchingChatPerformanceTest {

    private static final int ROOM_COUNT = 100;
    private static final int MESSAGE_PER_ROOM = 1_000;
    private static final int CHUNK_SIZE = 1_000;
    private static final int PAGE_SIZE = 50;
    private static final int TARGET_ROOM_INDEX = 49; // 50번째 방 (MongoDB 테스트의 roomId 50과 같은 위치)

    @Autowired
    private MatchingChatRepository matchingChatRepository;

    @Autowired
    private MatchingChatRoomRepository matchingChatRoomRepository;

    @Autowired
    private MatchingRepository matchingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private final List<MatchingChatRoom> rooms = new ArrayList<>();
    private User sender;

    @BeforeAll
    void setUpData() {
        // H2 테스트 설정이 섞이지 않고 MySQL로 접속했는지 확인
        log.info("[MySQL] 접속 URL: {}", datasourceUrl);
        assertThat(datasourceUrl).startsWith("jdbc:mysql:");

        sender = userRepository.save(
                User.builder()
                        .userId("perf-test-user")
                        .pw("dummy")
                        .build()
        );
        createRooms();
        insertMessages();
        jdbcTemplate.execute("ANALYZE TABLE matching_chat");
    }

    private void createRooms() {
        for (int i = 1; i <= ROOM_COUNT; i++) {
            // createdAt, updatedAt은 BaseEntity에서 자동으로 채워지므로 넣지 않는다
            Matching matching = matchingRepository.save(
                    Matching.builder()
                            .title("테스트 매칭 " + i)
                            .description("성능테스트용")
                            .maxPerson(10)
                            .meetTime(LocalDateTime.now().plusDays(1))
                            .build()
            );

            rooms.add(matchingChatRoomRepository.save(
                    MatchingChatRoom.builder()
                            .matching(matching)
                            .requester(sender)
                            .build()
            ));
        }
    }

    private void insertMessages() {
        long insertCountBefore = globalStatus("Com_insert");
        long start = System.nanoTime();

        List<MatchingChat> chunk = new ArrayList<>(CHUNK_SIZE);
        int inserted = 0;

        for (MatchingChatRoom room : rooms) {
            for (int i = 1; i <= MESSAGE_PER_ROOM; i++) {
                chunk.add(MatchingChat.builder()
                        .matchingChatRoom(room)
                        .sender(sender)
                        .message("테스트 메시지 " + i)
                        .build());

                if (chunk.size() == CHUNK_SIZE) {
                    // 청크마다 트랜잭션이 끝나므로 영속성 컨텍스트가 계속 쌓이지 않는다
                    matchingChatRepository.saveAllAndFlush(chunk);
                    inserted += chunk.size();
                    chunk.clear();
                }
            }
        }
        if (!chunk.isEmpty()) {
            matchingChatRepository.saveAllAndFlush(chunk);
            inserted += chunk.size();
        }

        // 시간을 먼저 계산해서 INSERT 수 조회 시간이 측정값에 섞이지 않게 한다
        long elapsedMs = QueryTimer.elapsedMillis(start);
        long insertStatements = globalStatus("Com_insert") - insertCountBefore;

        log.info("[MySQL] {}건 삽입 소요시간: {} ms", inserted, elapsedMs);
        log.info("[MySQL] 서버가 실행한 INSERT 문: {}회 (배치로 묶이면 약 {}회, 묶이지 않으면 약 {}회)",
                insertStatements, inserted / CHUNK_SIZE, inserted);
    }

    /**
     * MySQL 서버 전체에서 실행된 INSERT 문 수.
     * rewriteBatchedStatements가 동작하면 여러 행이 한 문장으로 묶여 1회로 집계된다.
     * 서버 전체 기준이므로 측정 중에는 로컬 앱을 따로 실행하지 않는다.
     */
    private long globalStatus(String name) {
        return jdbcTemplate.queryForObject(
                "SHOW GLOBAL STATUS LIKE '" + name + "'",
                (rs, rowNum) -> rs.getLong("Value"));
    }
    @Test
    void 채팅방_최근_50건_조회_평균_시간() {
        MatchingChatRoom targetRoom = rooms.get(TARGET_ROOM_INDEX);
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);
        String sql = "SELECT * FROM matching_chat WHERE chatroom_id = ? ORDER BY send_at DESC LIMIT 51";

        // 측정 전에 조회 결과가 맞는지 먼저 확인
        Slice<MatchingChat> result =
                matchingChatRepository.findByMatchingChatRoomOrderBySendAtDesc(targetRoom, pageable);
        assertThat(result.getContent()).hasSize(PAGE_SIZE);
        assertThat(result.hasNext()).isTrue();

        // 조회 1번에 SELECT가 몇 번 나가는지 확인
        long selectBefore = globalStatus("Com_select");
        matchingChatRepository.findByMatchingChatRoomOrderBySendAtDesc(targetRoom, pageable);
        log.info("[MySQL] 조회 1회당 SELECT 문: {}회", globalStatus("Com_select") - selectBefore);

        String hibernateSql = "SELECT mc.id, mc.chatroom_id, mc.message, mc.send_at, "
                + "s.id, s.email, s.name, s.nickname, s.pw, s.user_id "
                + "FROM matching_chat mc JOIN users s ON s.id = mc.sender_id "
                + "WHERE mc.chatroom_id = ? ORDER BY mc.send_at DESC LIMIT 51";
        List<String> plan = jdbcTemplate.queryForList(
                "EXPLAIN FORMAT=TREE " + hibernateSql, String.class, targetRoom.getId());
        log.info("[MySQL] 측정 시점 실행 계획:\n{}", String.join("\n", plan));
        // 1) JPA 레포지토리 조회
        double avg = QueryTimer.averageMillis(() ->
                matchingChatRepository.findByMatchingChatRoomOrderBySendAtDesc(targetRoom, pageable));
        log.info("[MySQL] 최근 {}건 조회 평균: {} ms (워밍업 {}회, 측정 {}회)",
                PAGE_SIZE, String.format("%.2f", avg), QueryTimer.WARMUP, QueryTimer.REPEAT);

        // 2) 같은 쿼리를 JDBC로 직접 실행
        double jdbcAvg = QueryTimer.averageMillis(() ->
                jdbcTemplate.queryForList(sql, targetRoom.getId()));
        log.info("[MySQL] JDBC 직접 실행 평균: {} ms", String.format("%.2f", jdbcAvg));

        // 3) 읽기 전용 트랜잭션 안에서 JDBC 실행
        TransactionTemplate readOnlyTx = new TransactionTemplate(transactionManager);
        readOnlyTx.setReadOnly(true);
        double readOnlyAvg = QueryTimer.averageMillis(() ->
                readOnlyTx.executeWithoutResult(status ->
                        jdbcTemplate.queryForList(sql, targetRoom.getId())));
        log.info("[MySQL] 읽기 전용 트랜잭션 + JDBC 평균: {} ms", String.format("%.2f", readOnlyAvg));

        // 4) 일반 트랜잭션 안에서 JDBC 실행
        TransactionTemplate normalTx = new TransactionTemplate(transactionManager);
        double normalAvg = QueryTimer.averageMillis(() ->
                normalTx.executeWithoutResult(status ->
                        jdbcTemplate.queryForList(sql, targetRoom.getId())));
        log.info("[MySQL] 일반 트랜잭션 + JDBC 평균: {} ms", String.format("%.2f", normalAvg));
    }
}

package com.exerciting.Exerciting.MatchingChat;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.mongo.MatchingChatDocument;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.mongo.MatchingChatDocumentRepository;
import com.exerciting.Exerciting.Support.QueryTimer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoDB 채팅 메시지 삽입/조회 성능 측정.
 * 100개 방 x 방당 1,000건 = 총 10만 건. 비교 대상: MatchingChatPerformanceTest
 *
 * MySQL 테스트와 같은 조건: 데이터 규모, 청크 크기, 최근 50건 Slice 조회, 워밍업 후 평균.
 */
@SpringBootTest
@ActiveProfiles({"local", "perf"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class MatchingChatMongoPerformanceTest {

    private static final int ROOM_COUNT = 100;
    private static final int MESSAGE_PER_ROOM = 1_000;
    private static final int CHUNK_SIZE = 1_000;
    private static final int PAGE_SIZE = 50;
    private static final long TARGET_ROOM_ID = 50L; // MySQL 테스트의 50번째 방과 같은 위치
    private static final String INDEX_NAME = "room_time_idx";

    @Autowired
    private MatchingChatDocumentRepository matchingChatDocumentRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeAll
    void setUpData() {
        // 문서만 지우고 인덱스는 유지된다
        matchingChatDocumentRepository.deleteAll();
        assertIndexExists();
        insertMessages();
    }

    private void assertIndexExists() {
        boolean exists = mongoTemplate.indexOps(MatchingChatDocument.class)
                .getIndexInfo()
                .stream()
                .anyMatch(index -> INDEX_NAME.equals(index.getName()));

        assertThat(exists)
                .as("%s 인덱스가 없습니다. spring.data.mongodb.auto-index-creation 설정을 확인하세요.", INDEX_NAME)
                .isTrue();
    }

    private void insertMessages() {
        long start = System.nanoTime();
        List<MatchingChatDocument> chunk = new ArrayList<>(CHUNK_SIZE);
        int inserted = 0;

        for (long roomId = 1; roomId <= ROOM_COUNT; roomId++) {
            for (int i = 1; i <= MESSAGE_PER_ROOM; i++) {
                chunk.add(MatchingChatDocument.builder()
                        .roomId(roomId)
                        .senderId(1L)
                        .message("테스트 메시지 " + i)
                        .build());

                if (chunk.size() == CHUNK_SIZE) {
                    matchingChatDocumentRepository.insert(chunk);
                    inserted += chunk.size();
                    chunk.clear();
                }
            }
        }
        if (!chunk.isEmpty()) {
            matchingChatDocumentRepository.insert(chunk);
            inserted += chunk.size();
        }

        log.info("[MongoDB] {}건 삽입 소요시간: {} ms", inserted, QueryTimer.elapsedMillis(start));
    }

    @Test
    void 채팅방_최근_50건_조회_평균_시간() {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        // 측정 전에 조회 결과가 맞는지 먼저 확인
        Slice<MatchingChatDocument> result =
                matchingChatDocumentRepository.findByRoomIdOrderBySendAtDesc(TARGET_ROOM_ID, pageable);
        assertThat(result.getContent()).hasSize(PAGE_SIZE);
        assertThat(result.hasNext()).isTrue();

        double avg = QueryTimer.averageMillis(() ->
                matchingChatDocumentRepository.findByRoomIdOrderBySendAtDesc(TARGET_ROOM_ID, pageable));

        log.info("[MongoDB] 최근 {}건 조회 평균: {} ms (워밍업 {}회, 측정 {}회)",
                PAGE_SIZE, String.format("%.2f", avg), QueryTimer.WARMUP, QueryTimer.REPEAT);
    }
}

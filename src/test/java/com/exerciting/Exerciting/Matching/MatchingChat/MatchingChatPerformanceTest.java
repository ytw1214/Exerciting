package com.exerciting.Exerciting.Matching.MatchingChat;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity.MatchingChat;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.repository.MatchingChatRepository;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.repository.MatchingChatRoomRepository;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.matching.matching.repository.MatchingRepository;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Slf4j
class MatchingChatPerformanceTest {

    @Autowired
    private MatchingChatRepository matchingChatRepository;

    @Autowired
    private MatchingChatRoomRepository matchingChatRoomRepository;

    @Autowired
    private MatchingRepository matchingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private static final int ROOM_COUNT = 100;
    private static final int MESSAGE_PER_ROOM = 1000;
    private static final int CHUNK_SIZE = 1000;

    private List<MatchingChatRoom> rooms;

    @BeforeEach
    void setUp() {
        long roomCount = matchingChatRoomRepository.count();
        long chatCount = matchingChatRepository.count();

        if (roomCount >= ROOM_COUNT && chatCount >= (long) ROOM_COUNT * MESSAGE_PER_ROOM) {
            rooms = matchingChatRoomRepository.findAll();
            log.info("기존 데이터 존재 - 삽입 스킵 (방 {}개, 메시지 {}건)", roomCount, chatCount);
            return;
        }

        // 데이터가 불완전하면 싹 지우고 다시 (FK 참조 역순)
        matchingChatRepository.deleteAll();
        matchingChatRoomRepository.deleteAll();
        matchingRepository.deleteAll();

        User testUser = userRepository.findByUserId("perf-test-user")
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .userId("perf-test-user")
                                .pw("dummy")
                                .build()
                ));

        rooms = new ArrayList<>();
        for (int i = 1; i <= ROOM_COUNT; i++) {
            Matching matching = matchingRepository.save(
                    Matching.builder()
                            .title("테스트 매칭 " + i)
                            .description("성능테스트용")
                            .maxPerson(10)
                            .meetTime(LocalDateTime.now().plusDays(1))
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build()
            );

            rooms.add(matchingChatRoomRepository.save(
                    MatchingChatRoom.builder()
                            .matching(matching)
                            .requester(testUser)
                            .build()
            ));
        }

        long insertStart = System.currentTimeMillis();
        List<MatchingChat> chunk = new ArrayList<>();
        int totalInserted = 0;

        for (MatchingChatRoom room : rooms) {
            for (int j = 1; j <= MESSAGE_PER_ROOM; j++) {
                chunk.add(MatchingChat.builder()
                        .matchingChatRoom(room)
                        .sender(testUser)
                        .message("테스트 메시지 " + j)
                        .build());

                if (chunk.size() >= CHUNK_SIZE) {
                    matchingChatRepository.saveAllAndFlush(chunk);
                    entityManager.clear();
                    chunk.clear();
                    totalInserted += CHUNK_SIZE;
                }
            }
        }
        if (!chunk.isEmpty()) {
            matchingChatRepository.saveAllAndFlush(chunk);
            entityManager.clear();
            totalInserted += chunk.size();
        }

        long insertEnd = System.currentTimeMillis();
        log.info("총 {}건 메시지 삽입 완료, 소요시간: {} ms", totalInserted, (insertEnd - insertStart));
    }

    @Test
    void 특정_채팅방_메시지_조회_속도_측정() {
        MatchingChatRoom targetRoom = rooms.get(49);
        Pageable pageable = PageRequest.of(0, 30); // 실제 화면에서 한 번에 보여주는 개수 기준

        // 워밍업 (커넥션/쿼리플랜 캐싱 영향 배제)
        matchingChatRepository.findByMatchingChatRoomOrderBySendAtDesc(targetRoom, pageable);

        long start = System.currentTimeMillis();
        Slice<MatchingChat> result = matchingChatRepository.findByMatchingChatRoomOrderBySendAtDesc(targetRoom, pageable);
        long end = System.currentTimeMillis();

        log.info("조회 결과 {}건, 소요시간 {} ms", result.getContent().size(), (end - start));
    }
}
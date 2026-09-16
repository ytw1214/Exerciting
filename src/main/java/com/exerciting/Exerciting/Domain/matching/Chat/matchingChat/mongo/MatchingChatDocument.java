package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.mongo;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB 비교 실험용 채팅 메시지 도큐먼트.
 * MySQL의 matching_chat 테이블과 같은 조회 조건(방 ID + 최신순)을 위해 복합 인덱스를 둔다.
 */
@Document(collection = "matching_chat")
@CompoundIndex(name = "room_time_idx", def = "{'roomId': 1, 'sendAt': -1}")
@Getter
@NoArgsConstructor
public class MatchingChatDocument {

    @Id
    private String id;

    private Long roomId;
    private Long senderId;
    private String message;
    private LocalDateTime sendAt;

    @Builder
    public MatchingChatDocument(Long roomId, Long senderId, String message) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.message = message;
        this.sendAt = LocalDateTime.now();
    }
}

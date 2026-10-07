package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(indexes = @Index(
        name = "idx_chat_room_send_at",
        columnList = "chatroom_id, send_at"))
public class MatchingChat {
    public static final int MAX_MESSAGE_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "matching_chat_seq")
    @SequenceGenerator(
            name = "matching_chat_seq",
            sequenceName = "matching_chat_seq",
            allocationSize = 1000
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chatroom_id")
    private MatchingChatRoom matchingChatRoom;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="sender_id")
    private User sender;

    // 기본값 VARCHAR(255)라 긴 메시지가 저장 실패했다. 기존 DB는 ddl-auto=update가 길이를 바꾸지 않으므로
    // ALTER TABLE matching_chat MODIFY message VARCHAR(1000); 을 직접 실행해야 한다.
    @Column(length = MAX_MESSAGE_LENGTH)
    private String message;

    private LocalDateTime sendAt;

    @Builder
    public MatchingChat(MatchingChatRoom matchingChatRoom, User sender, String message) {
        this.matchingChatRoom = matchingChatRoom;
        this.sender = sender;
        this.message = message;
        this.sendAt = LocalDateTime.now();
    }
}

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
public class MatchingChat {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="chatroom_id")
    private MatchingChatRoom matchingChatRoom;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="sender_id")
    private User sender;

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

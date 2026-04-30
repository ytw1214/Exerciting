package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Fetch;

import java.time.LocalDateTime;

@Entity
@Getter
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
    private boolean isRead;

    @Builder
    public MatchingChat(MatchingChatRoom matchingChatRoom, User sender, String message) {
        this.matchingChatRoom = matchingChatRoom;
        this.sender = sender;
        this.message = message;
        this.sendAt = LocalDateTime.now();
        this.isRead = false;
    }
    public void updateIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}

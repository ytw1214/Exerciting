package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity;

import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatoom.entity.MatchingChatRoom;
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
    
    private LocalDateTime sendAt;

    @Builder
    public MatchingChat(LocalDateTime sendAt) {
        this.sendAt = sendAt;
    }

}

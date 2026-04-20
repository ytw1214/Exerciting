package com.exerciting.Exerciting.Domain.matching.Chat.matchingChat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class MatchingChat {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime sendAt;

    @Builder
    public MatchingChat(LocalDateTime sendAt) {
        this.sendAt = sendAt;
    }

}

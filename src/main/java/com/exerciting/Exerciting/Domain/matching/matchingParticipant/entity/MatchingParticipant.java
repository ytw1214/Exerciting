package com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
public class MatchingParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="matching_id")
    private Matching matching;

    private LocalDateTime createdAt;

    @Builder
    public MatchingParticipant(User user, Matching matching, LocalDateTime createdAt) {
        this.user = user;
        this.matching = matching;
        this.createdAt = createdAt;
    }
}
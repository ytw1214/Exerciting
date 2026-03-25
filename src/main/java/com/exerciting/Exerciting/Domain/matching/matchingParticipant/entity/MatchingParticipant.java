package com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
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
    public MatchingParticipant(User user, Matching matching) {
        this.user = user;
        this.matching = matching;
        this.createdAt = LocalDateTime.now();
    }
    /*
    public static MatchingParticipant to(Matching matching, User user) {
        return ;
    }

     */
}
package com.exerciting.Exerciting.Domain.matching.entity;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.User.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="matching")
public class Matching {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private int maxPerson;
    private int currentPerson; // 현재 인원 필드 추가
    @ManyToOne(
            fetch = FetchType.LAZY
            //CascadeType = CascadeType.
            )
    @JoinColumn(name="game_id")
    private Game game;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;
    private LocalDateTime meetTime;
    private LocalDateTime createdAt;
    /*
    @Enumerated(EnumType.STRING)
    private MatchingStatus status;
     */
    @Builder
    public Matching(String title, String description, int maxPerson, int currentPerson, Game game, User user, LocalDateTime meetTime, LocalDateTime createdAt) {
        this.title = title;
        this.description = description;
        this.maxPerson = maxPerson;
        this.currentPerson = currentPerson;
        this.game = game;
        this.user = user;
        this.meetTime = meetTime;
        this.createdAt = createdAt;
    }
}

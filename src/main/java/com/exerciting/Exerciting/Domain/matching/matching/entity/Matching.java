package com.exerciting.Exerciting.Domain.matching.matching.entity;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.InvalidInputException;
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
    private LocalDateTime updatedAt;


    @Enumerated(EnumType.STRING)
    private MatchingStatus status;
    @Builder
    public Matching(String title, String description, int maxPerson, Game game, User user, LocalDateTime meetTime, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.title = title;
        this.description = description;
        this.maxPerson = maxPerson;
        this.game = game;
        this.user = user;
        this.meetTime = meetTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = MatchingStatus.RECRUITING;
    }

    public void update(String title, String description, int maxPerson, LocalDateTime meetTime) {
        if (maxPerson < 2) {
            throw new InvalidInputException();
        }

        this.title = title;
        this.description = description;
        this.maxPerson = maxPerson;
        this.meetTime = meetTime;
        this.updatedAt = LocalDateTime.now();
    }
    public void checkAndFull(long currentCount) {
        if(currentCount >= this.getMaxPerson()) {
            this.status = MatchingStatus.FULL;
        }
    }
    public void checkAndReopen(long currentCount) {
        if(this.status == MatchingStatus.FULL && currentCount < this.maxPerson) {
            this.status = MatchingStatus.RECRUITING;
        }
    }
    public void close() {
        if(this.status == MatchingStatus.CLOSED) {
            throw new InvalidInputException();
        }
        this.status = MatchingStatus.CLOSED;
    }

    public void reopen() {
        if(this.status==MatchingStatus.RECRUITING) {
            throw new InvalidInputException();
        }
        this.status = MatchingStatus.RECRUITING;
    }
    public boolean isRecruiting() {
        return this.status == MatchingStatus.RECRUITING;
    }
}

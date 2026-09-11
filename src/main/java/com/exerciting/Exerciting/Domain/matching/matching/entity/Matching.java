package com.exerciting.Exerciting.Domain.matching.matching.entity;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import com.exerciting.Exerciting.Infrastructure.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="matching")
public class Matching extends BaseEntity {
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

    @Enumerated(EnumType.STRING)
    private MatchingStatus status;

    @Version
    private Long version;
    @Builder
    public Matching(String title, String description, int maxPerson, Game game, User user, LocalDateTime meetTime) {
        this.title = title;
        this.description = description;
        this.maxPerson = maxPerson;
        this.game = game;
        this.user = user;
        this.meetTime = meetTime;
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
        if(this.status == MatchingStatus.CLOSED || isTerminal()) {
            throw new InvalidInputException();
        }
        this.status = MatchingStatus.CLOSED;
    }

    public void complete() {
        if(isTerminal()) {
            throw new InvalidInputException();
        }
        this.status = MatchingStatus.COMPLETED;
    }
    public void cancel() {
        if(this.status == MatchingStatus.COMPLETED) {
            throw new InvalidInputException();
        }
        this.status = MatchingStatus.CANCELLED;
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

    public boolean isTerminal() {
        return this.status == MatchingStatus.COMPLETED || this.status == MatchingStatus.CANCELLED;
    }
}

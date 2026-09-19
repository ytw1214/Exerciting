package com.exerciting.Exerciting.Domain.matching.matching.entity;

import com.exerciting.Exerciting.Domain.game.entity.Game;
import com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity.MatchingChatRoom;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.*;
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
    public static final int MIN_PERSON = 2;

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

    public void validateJoinable(long currentCount) {
        if (status != MatchingStatus.RECRUITING && status != MatchingStatus.FULL) {
            throw new MatchingNotRecruitingException();
        }
        if (currentCount >= maxPerson) {
            throw new MatchingFullException();
        }
    }
    public void refreshCapacityStatus(long currentCount) {
        if(status != MatchingStatus.RECRUITING && status != MatchingStatus.FULL) {
            return;
        }
        this.status = (currentCount >= maxPerson)
                ? MatchingStatus.FULL
                : MatchingStatus.RECRUITING;
    }
    public void update(String title, String description, int maxPerson, LocalDateTime meetTime, long currentCount) {
        if (isTerminal()) {
            throw new InvalidStateTransitionException();
        }
        if(maxPerson < MIN_PERSON || maxPerson < currentCount) {
            throw new InvalidCapacityException();
        }
        if(meetTime == null || meetTime.isBefore(LocalDateTime.now())) {
            throw new InvalidTimeException();
        }
        this.title = title;
        this.description = description;
        this.maxPerson = maxPerson;
        this.meetTime = meetTime;
        refreshCapacityStatus(currentCount);
    }
    public void close() {
        if(status != MatchingStatus.RECRUITING && status != MatchingStatus.FULL) {
            throw new InvalidStateTransitionException();
        }
        this.status = MatchingStatus.CLOSED;
    }
    public void reopen(long currentCount) {
        if(status != MatchingStatus.CLOSED) {
            throw new InvalidStateTransitionException();
        }
        if(currentCount >= maxPerson) {
            throw new MatchingFullException();
        }
        this.status = MatchingStatus.RECRUITING;
    }
    public void complete() {
        if(isTerminal()) {
            throw new InvalidStateTransitionException();
        }
        this.status = MatchingStatus.COMPLETED;
    }
    public void cancel() {
        if(isTerminal()) {
            throw new InvalidStateTransitionException();
        }
        this.status = MatchingStatus.CANCELLED;
    }
    public boolean isRecruiting() {
        return this.status == MatchingStatus.RECRUITING;
    }

    public boolean isTerminal() {
        return this.status == MatchingStatus.COMPLETED || this.status == MatchingStatus.CANCELLED;
    }
    public boolean isHost(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

}

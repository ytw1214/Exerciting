package com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Exception.InvalidInputException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime lastReadAt;

    @Builder
    public MatchingParticipant(User user, Matching matching, LocalDateTime createdAt) {
        this.user = user;
        this.matching = matching;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.status = ParticipantStatus.JOINED;   // ← 생성 시 무조건 JOINED
    }
    public void updateLastReadAt(LocalDateTime time) {
        this.lastReadAt = time;
    }
    public void leave() {                     // ← 추가
        if (this.status != ParticipantStatus.JOINED) {
            throw new InvalidInputException();
        }
        this.status = ParticipantStatus.LEFT;
    }

    public void markAttended() {              // ← 추가
        if (this.status != ParticipantStatus.JOINED) {
            throw new InvalidInputException();
        }
        this.status = ParticipantStatus.ATTENDED;
    }

    public void markNoShow() {                // ← 추가 (지금은 안 쓰지만 enum에 있으니)
        if (this.status != ParticipantStatus.JOINED) {
            throw new InvalidInputException();
        }
        this.status = ParticipantStatus.NO_SHOW;
    }
}
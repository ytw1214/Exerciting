package com.exerciting.Exerciting.Domain.matching.matchingParticipant.entity;

import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.user.entity.User;
import com.exerciting.Exerciting.Infrastructure.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class MatchingParticipant extends BaseEntity {
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
    private ParticipantStatus status;

    private LocalDateTime lastReadAt;

    @Builder
    public MatchingParticipant(User user, Matching matching) {
        this.user = user;
        this.matching = matching;
        this.status = ParticipantStatus.JOINED;
    }

    public void updateLastReadAt(LocalDateTime time) {
        this.lastReadAt = time;
    }

    public void leave() {
        this.status = ParticipantStatus.LEFT;
    }

    public void markAttended() {
        this.status = ParticipantStatus.ATTENDED;
    }

    public void markNoShow() {
        this.status = ParticipantStatus.NO_SHOW;
    }

    public boolean isActive() {
        return this.status == ParticipantStatus.JOINED;
    }
}
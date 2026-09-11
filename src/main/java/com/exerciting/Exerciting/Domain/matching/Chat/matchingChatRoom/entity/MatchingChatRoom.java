package com.exerciting.Exerciting.Domain.matching.Chat.matchingChatRoom.entity;

import com.exerciting.Exerciting.Domain.matching.Chat.MatchingChatStatus;
import com.exerciting.Exerciting.Domain.matching.matching.entity.Matching;
import com.exerciting.Exerciting.Domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class MatchingChatRoom {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="matching_id")
    private Matching matching;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id")
    private User requester;
    @Enumerated(EnumType.STRING)
    private MatchingChatStatus status;


    @Builder
    public MatchingChatRoom(Matching matching, User requester) {
        this.matching = matching;
        this.requester = requester;
        this.status = MatchingChatStatus.WAITING;
    }

    public void accept() {
        this.status = MatchingChatStatus.ACCEPTED;
    }
    public void rejected() {
        this.status = MatchingChatStatus.REJECTED;
    }
    public boolean isWaiting() {
        return this.status == MatchingChatStatus.WAITING;
    }
}

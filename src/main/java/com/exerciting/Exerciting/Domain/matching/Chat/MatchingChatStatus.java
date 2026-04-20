package com.exerciting.Exerciting.Domain.matching.Chat;

public enum MatchingChatStatus {
    ACCEPTED("수락"),
    WAITING("대기중"),
    REJECTED("거절");

    private String status;

    MatchingChatStatus(String status) {
        this.status = status;
    }
}

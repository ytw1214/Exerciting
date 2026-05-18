package com.exerciting.Exerciting.Domain.matching.matching.entity;

public enum MatchingStatus {
    RECRUITING("모집중"),
    FULL("인원 꽉 참"),
    CLOSED("모집 완료");

    private String status;

    MatchingStatus(String status) {
        this.status = status;
    }
    public String getMatchingStatus() {
        return status;
    }
}

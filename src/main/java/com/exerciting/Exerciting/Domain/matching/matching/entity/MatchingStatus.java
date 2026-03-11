package com.exerciting.Exerciting.Domain.matching.matching.entity;

public enum MatchingStatus {
    RECRUTING("모집중"),
    COMPLETED("모집 완료");

    private String status;

    MatchingStatus(String status) {
        this.status = status;
    }
    public String getMatchingStatus() {
        return status;
    }
}

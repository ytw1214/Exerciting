package com.exerciting.Exerciting.Domain.matching.matching.entity;

import java.util.List;

public enum MatchingStatus {
    RECRUITING("모집중"),
    FULL("인원 꽉 참"),
    CLOSED("모집 완료"),
    COMPLETED("경기 완료"),
    CANCELLED("경기 취소");

    /** 아직 끝나지 않은 매칭. 목록 노출·채팅·탈퇴 제한의 기준. */
    public static final List<MatchingStatus> ACTIVE_STATUSES = List.of(RECRUITING, FULL, CLOSED);

    private String status;

    MatchingStatus(String status) {
        this.status = status;
    }
    public String getMatchingStatus() {
        return status;
    }
}

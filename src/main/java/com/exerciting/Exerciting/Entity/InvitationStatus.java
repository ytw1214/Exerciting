package com.exerciting.Exerciting.Entity;

public enum InvitationStatus {
    WAITING("대기중"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨");

    private String Status;

    InvitationStatus(String status) {
        this.Status = status;
    }
    public String getInvitationStatus() {
        return Status;
    }
}

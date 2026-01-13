package com.exerciting.Exerciting.Entity;

public enum GameStatus {
    BEFORE(0,"시작 전"),
    PROCEEDING(1,"진행 중"),
    FINISHED(2, "경기 종료"),
    CANCELED(-1, "취소"),
    PAUSED(-2, "중단");

    private final int code;
    private final String status;

    GameStatus(int code, String status) {
        this.code = code;
        this.status = status;
    }

    public int getCode() {
        return code;
    }
    public String getStatus() {
        return status;
    }
}

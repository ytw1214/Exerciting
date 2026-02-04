package com.exerciting.Exerciting.Domain.global;

public enum SportType {
    BASEBALL("야구"),
    FOOTBALL("축구"),
    ESPORTS("E스포츠"),
    BASKETBALL("농구"),
    VOLLEYBALL("배구");

    private final String type;

    SportType(String type) {
        this.type = type;
    }

    public String getSportName() {
        return type;
    }
}

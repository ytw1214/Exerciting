package com.exerciting.Exerciting.Entity;

public enum SportType {
    BASEBALL("야구"),
    FOOTBALL("축구"),
    ESPORTS("E스포츠"),
    BASKETBALL("농구"),
    VOLLEYBALL("배구");

    private final String name;

    SportType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

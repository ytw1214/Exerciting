package com.exerciting.Exerciting.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private SportType sportType;
    private String homeTeam;
    private String awayTeam;
    @ManyToOne(fetch = FetchType.LAZY) // 이 부분이 빠지면 에러가 납니다!
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;
    private LocalDateTime gameStartTime;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private GameStatus gameStatus;


    @Builder
    public Game(SportType sportType, String homeTeam, String awayTeam, Stadium stadium, LocalDateTime gameStartTime, String title, String description) {
        this.sportType = sportType;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
        this.gameStartTime = gameStartTime;
        this.title = title;
        this.description = description;
    }

}

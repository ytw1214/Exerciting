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
    private String sportType;
    private String homeTeam;
    private String awayTeam;
    @ManyToOne(fetch = FetchType.LAZY) // 이 부분이 빠지면 에러가 납니다!
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;
    private LocalDateTime gameStartTime;
    @Enumerated(EnumType.STRING)
    private GameStatus gameStatus;


    @Builder
    public Game(String sportType, String homeTeam, String awayTeam, Stadium stadium, LocalDateTime gameStartTime, GameStatus gameStatus) {
        this.sportType = sportType;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
        this.gameStartTime = gameStartTime;
        this.gameStatus = gameStatus;
    }

}

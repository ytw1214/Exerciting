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
    @Enumerated(EnumType.STRING)
    private GameStatus gameStatus;


    @Builder
<<<<<<< HEAD
    public Game(SportType sportType, String homeTeam, String awayTeam, Stadium stadium, LocalDateTime gameStartTime, String title, String description) {
=======
    public Game(String sportType, String homeTeam, String awayTeam, Stadium stadium, LocalDateTime gameStartTime, GameStatus gameStatus) {
>>>>>>> e7a6db60a7c0ef7492a669e5d33960f5db9fffc2
        this.sportType = sportType;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
        this.gameStartTime = gameStartTime;
        this.gameStatus = gameStatus;
    }

}

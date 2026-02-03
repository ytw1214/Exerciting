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
    @Enumerated(EnumType.STRING)
    private SportType sportType;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;
    private LocalDateTime gameStartTime;
    @Enumerated(EnumType.STRING)
    private GameStatus gameStatus;


    @Builder
    public Game(SportType sportType, Team homeTeam, Team awayTeam, Stadium stadium, LocalDateTime gameStartTime, GameStatus gameStatus) {
        this.sportType = sportType;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
        this.gameStartTime = gameStartTime;
        this.gameStatus = gameStatus;
    }

}

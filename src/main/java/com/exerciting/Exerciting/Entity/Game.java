package com.exerciting.Exerciting.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Getter
@Builder
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
    private Stadium stadium;
    private LocalDateTime gameStartTime;
    private String title;
    private String description;

    private int maxviewer;
    private int currentviewer;

}

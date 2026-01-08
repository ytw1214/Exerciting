package com.exerciting.Exerciting.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="matching")
public class Matching {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    private int maxPerson;
    private int currentPerson; // 현재 인원 필드 추가
    private Game game;
    @ManyToOne
    private User host;
    private LocalDateTime meetTime;
    /*
    @Enumerated(EnumType.STRING)
    private MatchingStatus status;

     */

}

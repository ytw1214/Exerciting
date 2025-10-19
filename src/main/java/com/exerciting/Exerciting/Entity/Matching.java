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
    private String matchName;
    private String description;
    private int maxPerson;
    @Enumerated(EnumType.STRING)
    private Activity activity;
    @ManyToOne
    private User hostId;
    private LocalDateTime matchTime;
    private int currentPerson; // 현재 인원 필드 추가
    //@Enumerated(EnumType.STRING)
    //private MatchingStatus status;

}

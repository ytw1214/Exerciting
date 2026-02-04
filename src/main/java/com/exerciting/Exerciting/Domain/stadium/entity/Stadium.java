package com.exerciting.Exerciting.Domain.stadium.entity;


import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
//@AllArgsConstructor
@Table(name="Stadium")
public class Stadium {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double latitude;
    private double longitude;
    private String address;

    @Builder
    public Stadium(String name, double latitude, double longitude, String address) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }
}

package com.exerciting.Exerciting.Entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="Stadium")
public class Stadium {
    @Id
    private long id;
    private String name;
    private long latitude;
    private long longitude;
    private String address;
}

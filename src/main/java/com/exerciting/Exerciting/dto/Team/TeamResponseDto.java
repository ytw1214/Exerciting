package com.exerciting.Exerciting.dto.Team;

import com.exerciting.Exerciting.Entity.Player;
import com.exerciting.Exerciting.Entity.Stadium;
import com.exerciting.Exerciting.Entity.Team;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
@Getter
public class TeamResponseDto {
    private Long id;
    private String name;
    private String imgUrl;
    private Stadium stadium;
    private List<Player> player = new ArrayList<>();
    private String sportType;

    @Builder
    public TeamResponseDto(Long id, String name, String imgurl, Stadium stadium, List<Player> player, String sportType) {
        this.id = id;
        this.name = name;
        this.imgUrl = imgurl;
        this.stadium = stadium;
        this.player = player;
        this.sportType = sportType;
    }

    public static TeamResponseDto of(Team team) {
        return new TeamResponseDto(
                team.getId(),
                team.getName(),
                team.getImgUrl(),
                team.getStadium(),
                team.getPlayer(),
                team.getSportType()
        );
    }
}

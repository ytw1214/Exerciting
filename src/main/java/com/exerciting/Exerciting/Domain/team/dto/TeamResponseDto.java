package com.exerciting.Exerciting.Domain.team.dto;

import com.exerciting.Exerciting.Domain.player.entity.Player;
import com.exerciting.Exerciting.Domain.global.SportType;
import com.exerciting.Exerciting.Domain.stadium.entity.Stadium;
import com.exerciting.Exerciting.Domain.team.entity.Team;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
@Getter
@NoArgsConstructor
public class TeamResponseDto {
    private Long id;
    private String name;
    private String shortName;
    private String imgUrl;
    private Stadium stadium;
    private List<Player> player = new ArrayList<>();
    private SportType sportType;

    @Builder
    public TeamResponseDto(Long id, String name, String shortName, String imgurl, Stadium stadium, List<Player> player, SportType sportType) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.imgUrl = imgurl;
        this.stadium = stadium;
        this.player = player;
        this.sportType = sportType;
    }

    public static TeamResponseDto of(Team team) {
        return new TeamResponseDto(
                team.getId(),
                team.getName(),
                team.getShortName(),
                team.getImgUrl(),
                team.getStadium(),
                team.getPlayer(),
                team.getSportType()
        );
    }
}

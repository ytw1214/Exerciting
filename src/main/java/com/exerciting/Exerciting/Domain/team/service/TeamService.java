package com.exerciting.Exerciting.Domain.team.service;

import com.exerciting.Exerciting.Domain.team.entity.Team;
import com.exerciting.Exerciting.Domain.player.repository.PlayerRepository;
import com.exerciting.Exerciting.Domain.team.repository.TeamRepository;
import com.exerciting.Exerciting.Domain.team.dto.TeamResponseDto;
import org.springframework.stereotype.Service;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }
    public TeamResponseDto getTeam(String teamName) {
        Team team = teamRepository.findByName(teamName)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 팀이 존재하지 않습니다."));
        return TeamResponseDto.of(team);
    }

    public TeamResponseDto getTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 팀이 존재하지 않습니다."));

        return TeamResponseDto.of(team);
    }
}

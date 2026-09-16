package com.exerciting.Exerciting.Domain.team.service;

import com.exerciting.Exerciting.Domain.team.entity.Team;
import com.exerciting.Exerciting.Domain.team.repository.TeamRepository;
import com.exerciting.Exerciting.Domain.team.dto.TeamResponseDto;
import com.exerciting.Exerciting.Exception.TeamNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TeamService {
    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }
    public TeamResponseDto getTeam(String teamName) {
        Team team = teamRepository.findByName(teamName)
                    .orElseThrow(TeamNotFoundException::new);
        return TeamResponseDto.of(team);
    }

    public TeamResponseDto getTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        return TeamResponseDto.of(team);
    }
}

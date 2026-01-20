package com.exerciting.Exerciting.Service;

import com.exerciting.Exerciting.Entity.Team;
import com.exerciting.Exerciting.Repository.PlayerRepository;
import com.exerciting.Exerciting.Repository.TeamRepository;
import com.exerciting.Exerciting.dto.Team.TeamResponseDto;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TeamService {
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    public TeamService(TeamRepository teamRepository, PlayerRepository playerRepository) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
    }
    /*
    @Transactional
    public void insert() {
        Team team = Team.Builer()
                .name("삼성 라이온즈")
                .logoImage("//6ptotvmi5753.edge.naverncp.com/KBO_IMAGE/emblem/regular/2025/emblem_OB.png")
                .stadium()
    }


     */

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

package com.exerciting.Exerciting.Service;

import com.exerciting.Exerciting.Entity.Team;
import com.exerciting.Exerciting.Repository.PlayerRepository;
import com.exerciting.Exerciting.Repository.TeamRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
}

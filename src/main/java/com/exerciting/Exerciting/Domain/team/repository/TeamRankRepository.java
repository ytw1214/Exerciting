package com.exerciting.Exerciting.Domain.team.repository;

import com.exerciting.Exerciting.Domain.team.dto.TeamRankCrawlDto;
import com.exerciting.Exerciting.Domain.team.entity.TeamRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRankRepository extends JpaRepository<TeamRank, Long> {
    List<TeamRank> findByTeamNameContaining(String name);
    List<TeamRank> findByTeamName();
}

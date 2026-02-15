package com.exerciting.Exerciting.Domain.team.repository;

import com.exerciting.Exerciting.Domain.team.dto.TeamRankCrawlDto;
import com.exerciting.Exerciting.Domain.team.entity.TeamRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRankRepository extends JpaRepository<TeamRank, Long> {
    List<TeamRank> findByTeamNameContaining(String name);
    TeamRank findTeamByTeamName(String teamName);
    List<TeamRank> findTeamsByTeamName(String teamName);
    List<TeamRank> findAll();
    List<TeamRank> findAllByOrderByTeamRankAsc();
    //List<TeamRank> findAllByTeamNameContaining(List<String> list);
    List<TeamRank> findAllByTeamNameIn(List<String> teamNames);
}

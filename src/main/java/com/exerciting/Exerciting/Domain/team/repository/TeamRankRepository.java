package com.exerciting.Exerciting.Domain.team.repository;

import com.exerciting.Exerciting.Domain.team.entity.TeamRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRankRepository extends JpaRepository<TeamRank, Long> {
}

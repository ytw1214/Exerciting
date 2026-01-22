package com.exerciting.Exerciting.Repository;

import com.exerciting.Exerciting.Entity.TeamRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRankRepository extends JpaRepository<TeamRank, Long> {
    //List<TeamRank> saveAll(TeamRank ranking);
}

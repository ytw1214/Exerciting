package com.exerciting.Exerciting.Domain.team.repository;

import com.exerciting.Exerciting.Domain.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(@Param("name") String name);
    Optional<Team> findByShortName(String shortName);
}

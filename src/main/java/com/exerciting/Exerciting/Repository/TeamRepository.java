package com.exerciting.Exerciting.Repository;

import com.exerciting.Exerciting.Entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(@Param("name") String name);

}

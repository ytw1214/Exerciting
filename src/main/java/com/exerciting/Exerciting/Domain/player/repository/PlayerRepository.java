package com.exerciting.Exerciting.Domain.player.repository;

import com.exerciting.Exerciting.Domain.player.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {


}

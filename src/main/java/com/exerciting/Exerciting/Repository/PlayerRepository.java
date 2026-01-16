package com.exerciting.Exerciting.Repository;

import com.exerciting.Exerciting.Entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {


}

package com.exerciting.Exerciting.Repository;

import com.exerciting.Exerciting.Entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    List<Stadium> findByAddressContaining(String address);
    List<Stadium> findByNameContaining(String name);
    Optional<Stadium> findByLatitudeAndLongitude(double latitude, double longitude);
}

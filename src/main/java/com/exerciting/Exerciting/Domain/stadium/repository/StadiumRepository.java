package com.exerciting.Exerciting.Domain.stadium.repository;
import com.exerciting.Exerciting.Domain.stadium.entity.Stadium;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface StadiumRepository extends JpaRepository<Stadium, Long> {
    List<Stadium> findByAddressContaining(String address);
    List<Stadium> findByNameContaining(String name);
    Optional<Stadium> findByLatitudeAndLongitude(double latitude, double longitude);

}

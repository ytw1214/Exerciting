package com.exerciting.Exerciting.Domain.user.repository;

import com.exerciting.Exerciting.Domain.user.entity.RefreshToken;
import com.exerciting.Exerciting.Domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByUser(User user);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    void deleteByUser(User user);
}

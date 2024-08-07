package com.demo_quizz_1.repository;

import com.demo_quizz_1.entities.RefreshToken;
import com.demo_quizz_1.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    @Modifying
    int deleteByUser(User user);
}

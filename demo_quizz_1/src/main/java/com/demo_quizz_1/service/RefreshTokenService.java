package com.demo_quizz_1.service;

import com.demo_quizz_1.entities.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {
    Optional<RefreshToken> findByToken(String token);

    RefreshToken createRefreshToken(Long userId);

    RefreshToken verifyRefreshToken(RefreshToken token);

    int deleteByUserId(Long userId);
}

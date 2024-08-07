package com.demo_quizz_1.service;

import com.demo_quizz_1.entities.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface AuthService {
    User processOAuthPostLogin(OAuth2User oauth2User);
    String generateToken(User user);
}

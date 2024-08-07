package com.demo_quizz_1.controller;

import com.demo_quizz_1.entities.RefreshToken;
import com.demo_quizz_1.entities.User;
import com.demo_quizz_1.security.JwtAuthFilter;
import com.demo_quizz_1.security.JwtTokenProvider;
import com.demo_quizz_1.service.AuthService;
import com.demo_quizz_1.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    public JwtTokenProvider jwtTokenProvider;

//    @Qualifier("jwtTokenAuthFilter")

    @Autowired
    private JwtAuthFilter jwtTokenAuthFilter;

    @GetMapping("login-success")
    public ResponseEntity<?> loginSuccess(@AuthenticationPrincipal OAuth2User oAuth2User, Authentication authentication) {
        logger.info("loginSuccess method called");
//        logger.debug("Oauth2User: {}", oAuth2User);

        if(oAuth2User == null && authentication != null) {
            logger.info("oAuth2User is null, using Authentication");
            if(authentication.getPrincipal() instanceof OAuth2User) {
                oAuth2User = (OAuth2User) authentication.getPrincipal();
            }
        }

        if(oAuth2User == null) {
            logger.error("Unable to retrieve OAuth2User");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication Failed");
        }

        try {
            logger.info("Processing OAuth2User: {}", oAuth2User.getName());
            User user = authService.processOAuthPostLogin(oAuth2User);
            String token = authService.generateToken(user);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("refreshToken", refreshToken.getToken());
            response.put("user", user);

            logger.info("login successfully for user: {}", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error during login process", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during the login process");
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody String refreshToken) {
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyRefreshToken)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtTokenProvider.generateTokenFromUsername(user.getEmail());
                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    response.put("refreshToken", refreshToken);
                    return ResponseEntity.ok(response);
                })
                .orElseThrow(() -> new RuntimeException("Refresh token not found in database!"));
    }

    @GetMapping("/login-failure")
    public ResponseEntity<?> loginFailure() {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Login failed");
        return ResponseEntity.badRequest().body(response);
    }
}

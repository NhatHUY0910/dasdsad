package com.demo_quizz_1.service.impl;

import com.demo_quizz_1.entities.Role;
import com.demo_quizz_1.entities.User;
import com.demo_quizz_1.repository.RoleRepository;
import com.demo_quizz_1.repository.UserRepository;
import com.demo_quizz_1.security.JwtTokenProvider;
import com.demo_quizz_1.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public User processOAuthPostLogin(OAuth2User oauth2User) {
        logger.info("Processing OAuth post login");
        String email = oauth2User.getAttribute("email");
        logger.debug("OAuth2User email: {}", email);

        User user = userRepository.findByEmail(email);
        if (user == null) {
            logger.info("Creating new user for email: {}", email);
            user = new User();
            user.setEmail(email);
            user.setUsername(oauth2User.getAttribute("name"));

            Role studentRole = roleRepository.findByName("STUDENT");
            user.setRoles(Collections.singleton(studentRole));

            userRepository.save(user);
            logger.info("New user created with ID: {}", user.getId());
        } else {
            logger.info("Existing user found with ID: {}", user.getId());
        }
        return user;
    }

    @Override
    public String generateToken(User user) {
        logger.info("Generating token for user: {}", user.getEmail());
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
//                .toList();
                .collect(Collectors.toList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(authentication);
    }
}

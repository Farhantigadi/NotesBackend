package com.interviewprep.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null || !user.getPassword().equals(password)) {
            log.warn("[AUTH] Login failed for username: {}", username);
            throw new InvalidCredentialsException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(username);
        log.info("[AUTH] Login successful for username: {}", username);
        return token;
    }
}

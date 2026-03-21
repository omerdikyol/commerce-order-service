package com.commerce.orderservice.service;

import com.commerce.orderservice.dto.auth.AuthResponse;
import com.commerce.orderservice.dto.auth.LoginRequest;
import com.commerce.orderservice.dto.auth.RegisterRequest;
import com.commerce.orderservice.entity.User;
import com.commerce.orderservice.entity.enums.Role;
import com.commerce.orderservice.exception.ConflictException;
import com.commerce.orderservice.repository.UserRepository;
import com.commerce.orderservice.security.SecurityUserPrincipal;
import com.commerce.orderservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityUserPrincipal principal = (SecurityUserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();
        String token = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}

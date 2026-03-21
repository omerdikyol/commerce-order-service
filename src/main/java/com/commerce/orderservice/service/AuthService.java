package com.commerce.orderservice.service;

import com.commerce.orderservice.dto.auth.AuthResponse;
import com.commerce.orderservice.dto.auth.LoginRequest;
import com.commerce.orderservice.dto.auth.LogoutRequest;
import com.commerce.orderservice.dto.auth.RefreshTokenRequest;
import com.commerce.orderservice.dto.auth.RegisterRequest;
import com.commerce.orderservice.entity.RefreshToken;
import com.commerce.orderservice.entity.User;
import com.commerce.orderservice.entity.enums.Role;
import com.commerce.orderservice.exception.ConflictException;
import com.commerce.orderservice.exception.ResourceNotFoundException;
import com.commerce.orderservice.repository.RefreshTokenRepository;
import com.commerce.orderservice.repository.UserRepository;
import com.commerce.orderservice.security.SecurityUserPrincipal;
import com.commerce.orderservice.security.JwtService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
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
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException("Provided token is not a refresh token");
        }

        String tokenId = jwtService.extractTokenId(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByTokenIdAndRevokedFalse(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found or revoked"));

        if (jwtService.isTokenExpiredByDate(storedToken.getExpiresAt())) {
            storedToken.setRevoked(true);
            refreshTokenRepository.save(storedToken);
            throw new IllegalArgumentException("Refresh token has expired");
        }

        String username = jwtService.extractUsername(refreshToken);
        if (!storedToken.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Refresh token user mismatch");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        return issueTokens(storedToken.getUser());
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String refreshToken = request.getRefreshToken();
        String tokenType = jwtService.extractTokenType(refreshToken);
        if (!"refresh".equals(tokenType)) {
            throw new IllegalArgumentException("Provided token is not a refresh token");
        }

        String tokenId = jwtService.extractTokenId(refreshToken);
        refreshTokenRepository.findByTokenIdAndRevokedFalse(tokenId).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthResponse issueTokens(@NonNull User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenId = jwtService.generateRefreshTokenId();
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenId);

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .tokenId(refreshTokenId)
                .expiresAt(jwtService.calculateRefreshTokenExpiry())
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}

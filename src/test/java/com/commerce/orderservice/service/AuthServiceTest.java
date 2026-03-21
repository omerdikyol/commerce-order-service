package com.commerce.orderservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.commerce.orderservice.dto.auth.AuthResponse;
import com.commerce.orderservice.dto.auth.LoginRequest;
import com.commerce.orderservice.dto.auth.RefreshTokenRequest;
import com.commerce.orderservice.entity.RefreshToken;
import com.commerce.orderservice.entity.User;
import com.commerce.orderservice.entity.enums.Role;
import com.commerce.orderservice.exception.ResourceNotFoundException;
import com.commerce.orderservice.repository.RefreshTokenRepository;
import com.commerce.orderservice.repository.UserRepository;
import com.commerce.orderservice.security.JwtService;
import com.commerce.orderservice.security.SecurityUserPrincipal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(42L)
                .username("alice")
                .password("hashed")
                .email("alice@test.com")
                .role(Role.USER)
                .build();
    }

    @Test
    void login_returnsAccessAndRefreshTokens() {
        LoginRequest request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("pass");

        SecurityUserPrincipal principal = new SecurityUserPrincipal(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshTokenId()).thenReturn("rt-id");
        when(jwtService.generateRefreshToken(user, "rt-id")).thenReturn("refresh-token");
        when(jwtService.calculateRefreshTokenExpiry()).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse response = authService.login(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void refresh_rotatesTokenAndReturnsNewPair() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("old-refresh");

        RefreshToken stored = RefreshToken.builder()
                .id(1L)
                .user(user)
                .tokenId("old-rt-id")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(jwtService.extractTokenType("old-refresh")).thenReturn("refresh");
        when(jwtService.extractTokenId("old-refresh")).thenReturn("old-rt-id");
        when(refreshTokenRepository.findByTokenIdAndRevokedFalse("old-rt-id")).thenReturn(Optional.of(stored));
        when(jwtService.isTokenExpiredByDate(stored.getExpiresAt())).thenReturn(false);
        when(jwtService.extractUsername("old-refresh")).thenReturn("alice");
        when(jwtService.generateAccessToken(user)).thenReturn("new-access");
        when(jwtService.generateRefreshTokenId()).thenReturn("new-rt-id");
        when(jwtService.generateRefreshToken(user, "new-rt-id")).thenReturn("new-refresh");
        when(jwtService.calculateRefreshTokenExpiry()).thenReturn(LocalDateTime.now().plusDays(7));

        AuthResponse response = authService.refresh(request);

        assertEquals("new-access", response.accessToken());
        assertEquals("new-refresh", response.refreshToken());
        assertNotNull(response.userId());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void refresh_whenTokenMissing_throwsNotFound() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("missing-refresh");

        when(jwtService.extractTokenType("missing-refresh")).thenReturn("refresh");
        when(jwtService.extractTokenId("missing-refresh")).thenReturn("missing-id");
        when(refreshTokenRepository.findByTokenIdAndRevokedFalse("missing-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.refresh(request));
    }
}

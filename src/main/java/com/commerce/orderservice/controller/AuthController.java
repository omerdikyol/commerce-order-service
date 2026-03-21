package com.commerce.orderservice.controller;

import com.commerce.orderservice.dto.auth.AuthResponse;
import com.commerce.orderservice.dto.auth.LoginRequest;
import com.commerce.orderservice.dto.auth.RegisterRequest;
import com.commerce.orderservice.dto.common.ApiMessageResponse;
import com.commerce.orderservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and authenticate users")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered"),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "409", description = "Username/email conflict")
            }
    )
    public ResponseEntity<ApiMessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiMessageResponse("User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user and return JWT",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid credentials payload")
            }
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

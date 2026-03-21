package com.commerce.orderservice.dto.auth;

import com.commerce.orderservice.entity.enums.Role;
import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String username,
        Role role
) {
}

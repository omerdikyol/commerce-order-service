package com.commerce.orderservice.dto.product;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity
) {
}

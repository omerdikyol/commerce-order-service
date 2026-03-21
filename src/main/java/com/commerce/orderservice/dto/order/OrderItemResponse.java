package com.commerce.orderservice.dto.order;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal priceAtPurchase
) {
}

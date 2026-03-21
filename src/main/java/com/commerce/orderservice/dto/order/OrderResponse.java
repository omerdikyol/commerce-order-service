package com.commerce.orderservice.dto.order;

import com.commerce.orderservice.entity.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record OrderResponse(
        Long id,
        LocalDateTime orderDate,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items
) {
}

package com.commerce.orderservice.mapper;

import com.commerce.orderservice.dto.order.OrderItemResponse;
import com.commerce.orderservice.dto.order.OrderResponse;
import com.commerce.orderservice.entity.Order;
import com.commerce.orderservice.entity.OrderItem;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OrderMapper {

    public static OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(OrderMapper::toItemResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .build();
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .build();
    }
}

package com.commerce.orderservice.controller;

import com.commerce.orderservice.dto.order.OrderCreateRequest;
import com.commerce.orderservice.dto.order.OrderResponse;
import com.commerce.orderservice.security.SecurityUserPrincipal;
import com.commerce.orderservice.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(principal.getUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal SecurityUserPrincipal principal) {
        return ResponseEntity.ok(orderService.getUserOrders(principal.getUserId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getMyOrderById(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.getUserOrderById(principal.getUserId(), id));
    }
}

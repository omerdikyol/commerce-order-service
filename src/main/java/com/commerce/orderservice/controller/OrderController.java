package com.commerce.orderservice.controller;

import com.commerce.orderservice.dto.order.OrderCreateRequest;
import com.commerce.orderservice.dto.order.OrderResponse;
import com.commerce.orderservice.security.SecurityUserPrincipal;
import com.commerce.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Orders", description = "Order placement and user-specific order history")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order for the authenticated user")
    public ResponseEntity<OrderResponse> createOrder(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody OrderCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(principal.getUserId(), request));
    }

    @GetMapping
    @Operation(summary = "Get all orders for the authenticated user")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal SecurityUserPrincipal principal) {
        return ResponseEntity.ok(orderService.getUserOrders(principal.getUserId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single order owned by the authenticated user")
    public ResponseEntity<OrderResponse> getMyOrderById(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.getUserOrderById(principal.getUserId(), id));
    }
}

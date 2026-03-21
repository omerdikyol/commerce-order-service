package com.commerce.orderservice.service;

import com.commerce.orderservice.dto.order.OrderCreateRequest;
import com.commerce.orderservice.dto.order.OrderItemRequest;
import com.commerce.orderservice.dto.order.OrderResponse;
import com.commerce.orderservice.entity.Order;
import com.commerce.orderservice.entity.OrderItem;
import com.commerce.orderservice.entity.Product;
import com.commerce.orderservice.entity.User;
import com.commerce.orderservice.entity.enums.OrderStatus;
import com.commerce.orderservice.mapper.OrderMapper;
import com.commerce.orderservice.repository.OrderRepository;
import com.commerce.orderservice.repository.ProductRepository;
import com.commerce.orderservice.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse createOrder(Long userId, OrderCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + userId));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Product not found with id: " + itemRequest.getProductId()
                    ));

            int requestedQuantity = itemRequest.getQuantity();
            if (product.getStockQuantity() < requestedQuantity) {
                throw new IllegalStateException("Insufficient stock for product id: " + product.getId());
            }

            product.setStockQuantity(product.getStockQuantity() - requestedQuantity);

            BigDecimal itemPrice = product.getPrice().setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineTotal = itemPrice.multiply(BigDecimal.valueOf(requestedQuantity));
            totalAmount = totalAmount.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(requestedQuantity)
                    .priceAtPurchase(itemPrice)
                    .build();
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));

        Order savedOrder = orderRepository.save(order);
        return OrderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getUserOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Order not found for user with order id: " + orderId
                ));
        return OrderMapper.toResponse(order);
    }
}

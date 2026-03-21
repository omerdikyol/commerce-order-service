package com.commerce.orderservice.repository;

import com.commerce.orderservice.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    List<Order> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"orderItems", "orderItems.product"})
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
}

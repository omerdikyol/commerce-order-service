package com.commerce.orderservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.commerce.orderservice.dto.order.OrderCreateRequest;
import com.commerce.orderservice.dto.order.OrderItemRequest;
import com.commerce.orderservice.entity.Order;
import com.commerce.orderservice.entity.Product;
import com.commerce.orderservice.entity.User;
import com.commerce.orderservice.entity.enums.Role;
import com.commerce.orderservice.exception.InsufficientStockException;
import com.commerce.orderservice.repository.OrderRepository;
import com.commerce.orderservice.repository.ProductRepository;
import com.commerce.orderservice.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(10L)
                .username("alice")
                .password("hashed")
                .email("alice@test.com")
                .role(Role.USER)
                .build();

        product = Product.builder()
                .id(5L)
                .name("Keyboard")
                .price(new BigDecimal("25.00"))
                .stockQuantity(10)
                .build();
    }

    @Test
    void createOrder_deductsStockAndPersistsOrder() {
        OrderCreateRequest request = new OrderCreateRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(5L);
        item.setQuantity(2);
        request.setItems(List.of(item));

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0, Order.class);
            saved.setId(99L);
            return saved;
        });

        orderService.createOrder(10L, request);

        assertEquals(8, product.getStockQuantity());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertEquals(new BigDecimal("50.00"), orderCaptor.getValue().getTotalAmount());
        assertEquals(1, orderCaptor.getValue().getOrderItems().size());
    }

    @Test
    void createOrder_whenStockInsufficient_throwsAndDoesNotPersistOrder() {
        OrderCreateRequest request = new OrderCreateRequest();
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(5L);
        item.setQuantity(999);
        request.setItems(List.of(item));

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(10L, request));

        verify(orderRepository, never()).save(any(Order.class));
        assertEquals(10, product.getStockQuantity());
    }
}

package com.commerce.orderservice.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.commerce.orderservice.dto.order.OrderResponse;
import com.commerce.orderservice.dto.product.ProductResponse;
import com.commerce.orderservice.entity.User;
import com.commerce.orderservice.entity.enums.OrderStatus;
import com.commerce.orderservice.entity.enums.Role;
import com.commerce.orderservice.security.SecurityUserPrincipal;
import com.commerce.orderservice.service.OrderService;
import com.commerce.orderservice.service.ProductService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private OrderService orderService;

    @Test
    void userCannotCreateProduct() throws Exception {
        Authentication auth = authenticationFor(11L, "basic-user", Role.USER);

        mockMvc.perform(post("/api/products")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Mouse",
                                  "description":"Wireless",
                                  "price": 10.50,
                                  "stockQuantity": 5
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateProduct() throws Exception {
        Authentication auth = authenticationFor(1L, "admin", Role.ADMIN);
        ProductResponse response = ProductResponse.builder()
                .id(7L)
                .name("Mouse")
                .description("Wireless")
                .price(new BigDecimal("10.50"))
                .stockQuantity(5)
                .build();
        when(productService.createProduct(any())).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Mouse",
                                  "description":"Wireless",
                                  "price": 10.50,
                                  "stockQuantity": 5
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Mouse"));
    }

    @Test
    void getOrderUsesAuthenticatedUserScope() throws Exception {
        Authentication auth = authenticationFor(55L, "alice", Role.USER);
        OrderResponse response = OrderResponse.builder()
                .id(99L)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .items(List.of())
                .build();

        when(orderService.getUserOrderById(55L, 99L)).thenReturn(response);

        mockMvc.perform(get("/api/orders/99").with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.totalAmount").value(50.00));

        verify(orderService).getUserOrderById(eq(55L), eq(99L));
    }

    @Test
    void swaggerAndOpenApiEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists());

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    private Authentication authenticationFor(Long userId, String username, Role role) {
        User user = User.builder()
                .id(userId)
                .username(username)
                .password("secret")
                .email(username + "@test.com")
                .role(role)
                .build();
        SecurityUserPrincipal principal = new SecurityUserPrincipal(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }
}

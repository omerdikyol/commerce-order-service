package com.commerce.orderservice.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCreateRequest {

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}

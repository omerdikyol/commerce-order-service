package com.commerce.orderservice.mapper;

import com.commerce.orderservice.dto.product.ProductRequest;
import com.commerce.orderservice.dto.product.ProductResponse;
import com.commerce.orderservice.entity.Product;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProductMapper {

    public static Product toEntity(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .build();
    }

    public static void updateEntity(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
    }

    public static ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .build();
    }
}

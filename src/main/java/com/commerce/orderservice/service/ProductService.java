package com.commerce.orderservice.service;

import com.commerce.orderservice.dto.product.ProductRequest;
import com.commerce.orderservice.dto.product.ProductResponse;
import com.commerce.orderservice.entity.Product;
import com.commerce.orderservice.exception.ResourceNotFoundException;
import com.commerce.orderservice.mapper.ProductMapper;
import com.commerce.orderservice.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = ProductMapper.toEntity(request);
        return ProductMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        ProductMapper.updateEntity(product, request);
        return ProductMapper.toResponse(productRepository.save(product));
    }
}

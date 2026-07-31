package com.bookworm.service;

import com.bookworm.dto.admin.AdminProductResponse;
import com.bookworm.dto.admin.UpdateProductFlagsRequest;
import com.bookworm.entity.Product;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminProductService {

    private final ProductRepository productRepository;

    public AdminProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminProductResponse> listAll() {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(AdminProductResponse::from)
                .toList();
    }

    public AdminProductResponse updateFlags(Integer productId, UpdateProductFlagsRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        if (request.getIsFeatured() != null) product.setIsFeatured(request.getIsFeatured());
        if (request.getIsBestseller() != null) product.setIsBestseller(request.getIsBestseller());

        return AdminProductResponse.from(productRepository.save(product));
    }
}

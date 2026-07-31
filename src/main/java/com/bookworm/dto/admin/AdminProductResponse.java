package com.bookworm.dto.admin;

import com.bookworm.entity.Product;

import java.math.BigDecimal;

public record AdminProductResponse(
        Integer productId,
        String title,
        String titleEnglish,
        String categoryName,
        BigDecimal salePrice,
        Boolean isActive,
        Boolean isFeatured,
        Boolean isBestseller) {

    public static AdminProductResponse from(Product product) {
        return new AdminProductResponse(
                product.getProductId(),
                product.getTitle(),
                product.getTitleEnglish(),
                product.getCategory().getCategoryName(),
                product.getSalePrice(),
                product.getIsActive(),
                product.getIsFeatured(),
                product.getIsBestseller());
    }
}

package com.bookworm.dto.catalog;

import com.bookworm.entity.Product;

import java.math.BigDecimal;

public record ProductSummaryResponse(
        Integer productId,
        Integer categoryId,
        Integer genreId,
        String title,
        String titleEnglish,
        String coverImageUrl,
        BigDecimal salePrice,
        Boolean isRentable,
        Boolean isLendable) {

    public static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(
                product.getProductId(),
                product.getCategory().getCategoryId(),
                product.getGenre().getGenreId(),
                product.getTitle(),
                product.getTitleEnglish(),
                product.getCoverImageUrl(),
                product.getSalePrice(),
                product.getIsRentable(),
                product.getIsLendable());
    }
}

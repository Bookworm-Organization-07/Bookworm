package com.bookworm.dto.catalog;

import com.bookworm.entity.Product;

import java.math.BigDecimal;

public record ProductDetailResponse(
        Integer productId,
        Integer categoryId,
        Integer genreId,
        String genreName,
        Integer languageId,
        String languageName,
        String title,
        String titleEnglish,
        String description,
        String coverImageUrl,
        BigDecimal salePrice,
        Integer pages,
        Integer durationMinutes,
        String fileType,
        BigDecimal rentPricePerDay,
        Boolean isRentable,
        Boolean isLendable,
        Boolean isFeatured,
        Boolean isBestseller) {

    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getProductId(),
                product.getCategory().getCategoryId(),
                product.getGenre().getGenreId(),
                product.getGenre().getGenreName(),
                product.getLanguage().getLanguageId(),
                product.getLanguage().getLanguageName(),
                product.getTitle(),
                product.getTitleEnglish(),
                product.getDescription(),
                product.getCoverImageUrl(),
                product.getSalePrice(),
                product.getPages(),
                product.getDurationMinutes(),
                product.getFileType(),
                product.getRentPricePerDay(),
                product.getIsRentable(),
                product.getIsLendable(),
                product.getIsFeatured(),
                product.getIsBestseller());
    }
}

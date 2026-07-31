package com.bookworm.dto.catalog;

import com.bookworm.entity.ProductCategory;

public record CategoryResponse(Integer categoryId, String categoryName, String iconUrl, Integer displayOrder) {

    public static CategoryResponse from(ProductCategory category) {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getIconUrl(),
                category.getDisplayOrder());
    }
}

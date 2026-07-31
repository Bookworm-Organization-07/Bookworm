package com.bookworm.service;

import com.bookworm.dto.catalog.*;
import com.bookworm.entity.Product;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.GenreRepository;
import com.bookworm.repository.LanguageRepository;
import com.bookworm.repository.ProductCategoryRepository;
import com.bookworm.repository.ProductRepository;
import com.bookworm.repository.ProductSpecifications;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final ProductCategoryRepository categoryRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final ProductRepository productRepository;

    public CatalogService(
            ProductCategoryRepository categoryRepository,
            GenreRepository genreRepository,
            LanguageRepository languageRepository,
            ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder")).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public List<GenreResponse> listGenresForCategory(Integer categoryId) {
        return genreRepository.findByCategory_CategoryIdOrderByDisplayOrderAsc(categoryId).stream()
                .map(GenreResponse::from)
                .toList();
    }

    public List<LanguageResponse> listLanguages() {
        return languageRepository.findAll().stream().map(LanguageResponse::from).toList();
    }

    public List<ProductSummaryResponse> searchProducts(
            Integer categoryId,
            Integer genreId,
            String search,
            Boolean featured,
            Boolean bestseller,
            Boolean lendable,
            Boolean rentable,
            Integer size) {

        Specification<Product> spec = ProductSpecifications.isActive();
        if (categoryId != null) spec = spec.and(ProductSpecifications.hasCategory(categoryId));
        if (genreId != null) spec = spec.and(ProductSpecifications.hasGenre(genreId));
        if (Boolean.TRUE.equals(featured)) spec = spec.and(ProductSpecifications.isFeatured());
        if (Boolean.TRUE.equals(bestseller)) spec = spec.and(ProductSpecifications.isBestseller());
        if (Boolean.TRUE.equals(lendable)) spec = spec.and(ProductSpecifications.isLendable());
        if (Boolean.TRUE.equals(rentable)) spec = spec.and(ProductSpecifications.isRentable());
        if (search != null && !search.isBlank()) spec = spec.and(ProductSpecifications.matchesSearchTerm(search));

        int pageSize = (size != null && size > 0) ? size : 100;
        return productRepository.findAll(spec, PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent()
                .stream()
                .map(ProductSummaryResponse::from)
                .toList();
    }

    public ProductDetailResponse getProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        return ProductDetailResponse.from(product);
    }
}

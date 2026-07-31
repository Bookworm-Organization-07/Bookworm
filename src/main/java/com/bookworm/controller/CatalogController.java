package com.bookworm.controller;

import com.bookworm.dto.catalog.*;
import com.bookworm.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public List<CategoryResponse> listCategories() {
        return catalogService.listCategories();
    }

    @GetMapping("/categories/{categoryId}/genres")
    public List<GenreResponse> listGenres(@PathVariable Integer categoryId) {
        return catalogService.listGenresForCategory(categoryId);
    }

    @GetMapping("/languages")
    public List<LanguageResponse> listLanguages() {
        return catalogService.listLanguages();
    }

    @GetMapping("/products")
    public List<ProductSummaryResponse> searchProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) Boolean bestseller,
            @RequestParam(required = false) Boolean lendable,
            @RequestParam(required = false) Boolean rentable,
            @RequestParam(required = false) Integer size) {
        return catalogService.searchProducts(categoryId, genreId, search, featured, bestseller, lendable, rentable, size);
    }

    @GetMapping("/products/{productId}")
    public ProductDetailResponse getProduct(@PathVariable Integer productId) {
        return catalogService.getProduct(productId);
    }
}

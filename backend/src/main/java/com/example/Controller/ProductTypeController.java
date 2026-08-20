package com.example.Controller;

import com.example.Repository.ProductTypeRepository;
import com.example.models.Product_Type;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The list of product categories (eBook, Music, Audio-Book, Film, ...)
 * shown in the "Products" dropdown menu - see BRD section 4.1 Top Menu
 * Bar: "Products: submenu in which all product categories (eBook,
 * Music, Videos etc) will appear."
 */
@RestController
@RequestMapping("/api/product-types")
public class ProductTypeController {

    private final ProductTypeRepository productTypeRepository;

    public ProductTypeController(ProductTypeRepository productTypeRepository) {
        this.productTypeRepository = productTypeRepository;
    }

    @GetMapping
    public List<Product_Type> getAllProductTypes() {
        return productTypeRepository.findAll();
    }
}

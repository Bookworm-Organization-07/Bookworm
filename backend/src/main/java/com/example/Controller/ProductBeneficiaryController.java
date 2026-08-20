package com.example.Controller;

import com.example.Services.ProductBeneficiaryService;
import com.example.dto.ProductBeneficiaryDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Who has earned what in royalties. Admin only - see SecurityConfig. */
@RestController
@RequestMapping("/api/product-beneficiaries")
public class ProductBeneficiaryController {

    private final ProductBeneficiaryService service;

    public ProductBeneficiaryController(ProductBeneficiaryService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductBeneficiaryDTO> getAll() {
        return service.getAllProductBeneficiaries();
    }
}

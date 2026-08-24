package com.example.Repository;

import com.example.models.ProductBeneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductBeneficiaryRepository extends JpaRepository<ProductBeneficiary, Integer> {

    List<ProductBeneficiary> findByBeneficiary_BeneficiaryIdAndProduct_ProductId(int beneficiaryId, int productId);
}

package com.bookworm.repository;

import com.bookworm.entity.ProductStakeholder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductStakeholderRepository extends JpaRepository<ProductStakeholder, Integer> {

    List<ProductStakeholder> findByProduct_ProductId(Integer productId);
}

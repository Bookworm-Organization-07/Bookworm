package com.example.Repository;

import com.example.models.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Integer> {

    @Transactional
    void deleteByProduct_ProductId(int productId);
}

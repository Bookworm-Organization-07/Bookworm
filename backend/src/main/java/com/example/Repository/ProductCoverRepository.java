package com.example.Repository;

import com.example.models.ProductCover;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductCoverRepository extends JpaRepository<ProductCover, Integer> {

    Optional<ProductCover> findByProduct_ProductId(int productId);
}

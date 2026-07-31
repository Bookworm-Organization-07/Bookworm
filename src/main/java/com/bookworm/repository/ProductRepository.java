package com.bookworm.repository;

import com.bookworm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    List<Product> findByIsFeaturedTrueAndIsActiveTrue();

    List<Product> findByIsBestsellerTrueAndIsActiveTrue();

    List<Product> findByIsLendableTrueAndIsActiveTrue();

    List<Product> findByIsRentableTrueAndIsActiveTrue();

    Optional<Product> findByTitleIgnoreCaseAndCategory_CategoryId(String title, Integer categoryId);
}

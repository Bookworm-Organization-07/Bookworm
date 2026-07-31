package com.bookworm.repository;

import com.bookworm.entity.ProductTag;
import com.bookworm.entity.ProductTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductTagRepository extends JpaRepository<ProductTag, ProductTagId> {

    List<ProductTag> findByProduct_ProductId(Integer productId);
}

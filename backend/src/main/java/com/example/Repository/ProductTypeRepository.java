package com.example.Repository;

import com.example.models.Product_Type;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductTypeRepository extends JpaRepository<Product_Type, Integer> {

    Optional<Product_Type> findByTypeDescIgnoreCase(String typeDesc);
}

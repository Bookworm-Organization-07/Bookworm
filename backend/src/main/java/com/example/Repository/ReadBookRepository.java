package com.example.Repository;

import com.example.models.ReadBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadBookRepository extends JpaRepository<ReadBook, Integer> {

    Optional<ReadBook> findByProduct_ProductId(int productId);
}

package com.bookworm.repository;

import com.bookworm.entity.Shelf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShelfRepository extends JpaRepository<Shelf, Integer> {

    List<Shelf> findByUser_UserIdOrderByAcquiredAtDesc(Integer userId);
}

package com.bookworm.repository;

import com.bookworm.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
}

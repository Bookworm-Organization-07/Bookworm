package com.bookworm.repository;

import com.bookworm.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByOrder_OrderId(Integer orderId);
}

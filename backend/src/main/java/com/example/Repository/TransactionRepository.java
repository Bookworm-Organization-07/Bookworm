package com.example.Repository;

import com.example.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);
}

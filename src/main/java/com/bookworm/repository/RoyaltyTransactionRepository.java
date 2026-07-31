package com.bookworm.repository;

import com.bookworm.entity.RoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoyaltyTransactionRepository extends JpaRepository<RoyaltyTransaction, Integer> {

    List<RoyaltyTransaction> findByProductStakeholder_ProductStakeholderId(Integer productStakeholderId);
}

package com.example.Repository;

import com.example.models.LibraryPackagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LibraryPackagePurchaseRepository
        extends JpaRepository<LibraryPackagePurchase, Integer> {

    List<LibraryPackagePurchase> findByUser_UserIdOrderByPurchaseDateDesc(Integer userId);
}

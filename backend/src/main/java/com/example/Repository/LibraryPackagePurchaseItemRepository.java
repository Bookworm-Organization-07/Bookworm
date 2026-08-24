package com.example.Repository;

import com.example.models.LibraryPackagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface LibraryPackagePurchaseItemRepository
        extends JpaRepository<com.example.models.LibraryPackagePurchaseItem, Integer> {

    @Transactional
    void deleteByPurchase(LibraryPackagePurchase purchase);
}

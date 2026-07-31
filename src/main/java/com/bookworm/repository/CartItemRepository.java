package com.bookworm.repository;

import com.bookworm.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    List<CartItem> findByCart_CartId(Integer cartId);

    Optional<CartItem> findByCart_CartIdAndProduct_ProductId(Integer cartId, Integer productId);

    Optional<CartItem> findByCart_CartIdAndLibraryPackage_LibraryPackageId(Integer cartId, Integer libraryPackageId);

    void deleteByCart_CartId(Integer cartId);
}

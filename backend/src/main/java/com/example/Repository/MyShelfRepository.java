package com.example.Repository;

import com.example.models.MyShelf;
import com.example.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MyShelfRepository extends JpaRepository<MyShelf, Integer> {

    List<MyShelf> findByUser_UserId(Integer userId);

    boolean existsByUser_UserIdAndProduct_ProductId(Integer userId, Integer productId);

    @Transactional
    void deleteByUser(User user);
}

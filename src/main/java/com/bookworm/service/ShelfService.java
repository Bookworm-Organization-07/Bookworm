package com.bookworm.service;

import com.bookworm.dto.shelf.ShelfItemResponse;
import com.bookworm.entity.User;
import com.bookworm.repository.ShelfRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ShelfService {

    private final ShelfRepository shelfRepository;

    public ShelfService(ShelfRepository shelfRepository) {
        this.shelfRepository = shelfRepository;
    }

    public List<ShelfItemResponse> listShelf(User user) {
        return shelfRepository.findByUser_UserIdOrderByAcquiredAtDesc(user.getUserId()).stream()
                .map(ShelfItemResponse::from)
                .toList();
    }
}

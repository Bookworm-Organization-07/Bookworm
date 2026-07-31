package com.bookworm.controller;

import com.bookworm.dto.shelf.ShelfItemResponse;
import com.bookworm.entity.User;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.UserRepository;
import com.bookworm.service.ShelfService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shelf")
public class ShelfController {

    private final ShelfService shelfService;
    private final UserRepository userRepository;

    public ShelfController(ShelfService shelfService, UserRepository userRepository) {
        this.shelfService = shelfService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<ShelfItemResponse> getShelf(Authentication authentication) {
        User user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
        return shelfService.listShelf(user);
    }
}

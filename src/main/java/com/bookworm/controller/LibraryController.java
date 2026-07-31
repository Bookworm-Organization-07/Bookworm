package com.bookworm.controller;

import com.bookworm.dto.library.LendRequest;
import com.bookworm.dto.library.LibraryEntryResponse;
import com.bookworm.dto.library.LibraryPackageResponse;
import com.bookworm.dto.library.MyLibraryResponse;
import com.bookworm.entity.User;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.UserRepository;
import com.bookworm.service.LibraryService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/library")
public class LibraryController {

    private final LibraryService libraryService;
    private final UserRepository userRepository;

    public LibraryController(LibraryService libraryService, UserRepository userRepository) {
        this.libraryService = libraryService;
        this.userRepository = userRepository;
    }

    @GetMapping("/packages")
    public List<LibraryPackageResponse> listPackages() {
        return libraryService.listPackages();
    }

    @PostMapping("/lend")
    public LibraryEntryResponse lend(@Valid @RequestBody LendRequest request, Authentication authentication) {
        return libraryService.lend(currentUser(authentication), request.getProductId());
    }

    @GetMapping("/mine")
    public MyLibraryResponse mine(Authentication authentication) {
        return libraryService.getMyLibrary(currentUser(authentication));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}

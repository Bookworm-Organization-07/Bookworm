package com.example.Controller;

import com.example.Services.MyLibraryService;
import com.example.dto.MyLibraryItemResponseDto;
import com.example.security.CurrentUser;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my-library")
public class MyLibraryController {

    private final MyLibraryService myLibraryService;
    private final CurrentUser currentUser;

    public MyLibraryController(MyLibraryService myLibraryService, CurrentUser currentUser) {
        this.myLibraryService = myLibraryService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public ResponseEntity<List<MyLibraryItemResponseDto>> getUserLibrary() {
        return ResponseEntity.ok(
                myLibraryService.getUserLibrary(currentUser.require().getUserId()));
    }

    /** Streams the PDF, but only if the caller currently has it borrowed. */
    @GetMapping("/read/{productId}")
    public ResponseEntity<byte[]> readBook(@PathVariable Integer productId) {
        byte[] pdf = myLibraryService.readLibraryBook(
                currentUser.require().getUserId(), productId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}

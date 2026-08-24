package com.example.Controller;

import com.example.Services.ReadBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class ReadBookController {

    private final ReadBookService readBookService;

    public ReadBookController(ReadBookService readBookService) {
        this.readBookService = readBookService;
    }

    @PostMapping("/{productId}/upload")
    public ResponseEntity<Map<String, String>> uploadPdf(@PathVariable int productId,
                                                         @RequestParam("file") MultipartFile file)
            throws IOException {
        readBookService.savePdf(file, productId);
        return ResponseEntity.ok(Map.of("message", "PDF uploaded successfully."));
    }
}

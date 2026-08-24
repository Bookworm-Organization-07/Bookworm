package com.example.Controller;

import com.example.Services.LibraryPackageService;
import com.example.models.LibraryPackage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/library-packages")
public class LibraryPackageController {

    private final LibraryPackageService libraryPackageService;

    public LibraryPackageController(LibraryPackageService libraryPackageService) {
        this.libraryPackageService = libraryPackageService;
    }

    @GetMapping
    public ResponseEntity<List<LibraryPackage>> getAllPackages() {
        return ResponseEntity.ok(libraryPackageService.getAllPackages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibraryPackage> getPackageById(@PathVariable Integer id) {
        return ResponseEntity.ok(libraryPackageService.getPackageById(id));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<LibraryPackage> getPackageByName(@PathVariable String name) {
        return ResponseEntity.ok(libraryPackageService.getPackageByName(name));
    }

    @PostMapping
    public ResponseEntity<LibraryPackage> createPackage(@RequestBody LibraryPackage libraryPackage) {
        return new ResponseEntity<>(
                libraryPackageService.createPackage(libraryPackage), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Integer id) {
        libraryPackageService.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}

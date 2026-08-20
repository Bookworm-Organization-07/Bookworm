package com.example.Services;

import com.example.Repository.LibraryPackageRepository;
import com.example.models.LibraryPackage;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibraryPackageServiceImpl implements LibraryPackageService {

    private final LibraryPackageRepository libraryPackageRepository;

    public LibraryPackageServiceImpl(LibraryPackageRepository libraryPackageRepository) {
        this.libraryPackageRepository = libraryPackageRepository;
    }

    @Override
    public LibraryPackage createPackage(LibraryPackage libraryPackage) {
        if (libraryPackageRepository.existsByName(libraryPackage.getName())) {
            throw new IllegalStateException(
                    "A package named " + libraryPackage.getName() + " already exists.");
        }
        return libraryPackageRepository.save(libraryPackage);
    }

    @Override
    public LibraryPackage getPackageById(Integer packageId) {
        return libraryPackageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Library package not found."));
    }

    @Override
    public List<LibraryPackage> getAllPackages() {
        return libraryPackageRepository.findAll();
    }

    @Override
    public LibraryPackage getPackageByName(String name) {
        return libraryPackageRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Library package not found."));
    }

    @Override
    public void deletePackage(Integer packageId) {
        libraryPackageRepository.deleteById(packageId);
    }
}

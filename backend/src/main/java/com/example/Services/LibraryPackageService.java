package com.example.Services;

import com.example.models.LibraryPackage;

import java.util.List;

public interface LibraryPackageService {

    LibraryPackage createPackage(LibraryPackage libraryPackage);

    LibraryPackage getPackageById(Integer packageId);

    List<LibraryPackage> getAllPackages();

    LibraryPackage getPackageByName(String name);

    void deletePackage(Integer packageId);
}

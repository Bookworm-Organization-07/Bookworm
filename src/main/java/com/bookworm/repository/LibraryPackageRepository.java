package com.bookworm.repository;

import com.bookworm.entity.LibraryPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibraryPackageRepository extends JpaRepository<LibraryPackage, Integer> {

    Optional<LibraryPackage> findByPackageNameIgnoreCase(String packageName);

    List<LibraryPackage> findByIsActiveTrue();
}

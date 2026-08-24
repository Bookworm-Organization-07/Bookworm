package com.example.Repository;

import com.example.models.LibraryPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LibraryPackageRepository extends JpaRepository<LibraryPackage, Integer> {

    Optional<LibraryPackage> findByName(String name);

    boolean existsByName(String name);
}

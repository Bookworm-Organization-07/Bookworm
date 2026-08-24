package com.example.Repository;

import com.example.models.Genere;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenereRepository extends JpaRepository<Genere, Integer> {

    /** Used by the bulk uploader to reuse an existing genre instead of creating a duplicate. */
    Optional<Genere> findByGenereDescIgnoreCase(String genereDesc);
}

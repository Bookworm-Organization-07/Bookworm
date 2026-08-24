package com.example.Repository;

import com.example.models.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, Integer> {

    /** Used by the bulk uploader to reuse an existing language instead of creating a duplicate. */
    Optional<Language> findByLanguageDescIgnoreCase(String languageDesc);
}

package com.example.Repository;

import com.example.models.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    /**
     * Matches a search word against EITHER the title or its English
     * name, so a book titled in Marathi/Hindi/Konkani script can still be
     * found by searching for its English name. Call it with the same
     * search text for both parameters.
     */
    List<Product> findByProductNameContainingIgnoreCaseOrProductNameEnglishContainingIgnoreCase(
            String productName,
            String productNameEnglish,
            Pageable pageable
    );

    /**
     * Used by the bulk uploader to skip a row whose title is already in
     * the catalogue - returning the existing row (not just true/false)
     * so a cover_image on that row can still be attached to it, even
     * though the row itself is not created again.
     */
    Optional<Product> findByProductNameIgnoreCase(String productName);

    /** Titles that can be borrowed with a library package. */
    List<Product> findByLibraryTrue();

    List<Product> findByGenere_GenereDescAndLibraryTrue(String genereDesc);

    List<Product> findByLanguage_LanguageDescAndLibraryTrue(String languageDesc);

    List<Product> findByGenere_GenereDescAndLanguage_LanguageDescAndLibraryTrue(
            String genereDesc,
            String languageDesc
    );

    /** Catalogue browsing - all titles, not just the library-eligible ones. */
    List<Product> findByGenere_GenereDesc(String genereDesc);

    List<Product> findByLanguage_LanguageDesc(String languageDesc);

    List<Product> findByGenere_GenereDescAndLanguage_LanguageDesc(
            String genereDesc,
            String languageDesc
    );
}

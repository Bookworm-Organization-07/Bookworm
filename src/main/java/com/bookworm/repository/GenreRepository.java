package com.bookworm.repository;

import com.bookworm.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

    List<Genre> findByCategory_CategoryIdOrderByDisplayOrderAsc(Integer categoryId);

    Optional<Genre> findByCategory_CategoryIdAndGenreNameIgnoreCase(Integer categoryId, String genreName);
}

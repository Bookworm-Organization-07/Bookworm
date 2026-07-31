package com.bookworm.dto.catalog;

import com.bookworm.entity.Genre;

public record GenreResponse(Integer genreId, Integer categoryId, String genreName, Integer displayOrder) {

    public static GenreResponse from(Genre genre) {
        return new GenreResponse(
                genre.getGenreId(),
                genre.getCategory().getCategoryId(),
                genre.getGenreName(),
                genre.getDisplayOrder());
    }
}

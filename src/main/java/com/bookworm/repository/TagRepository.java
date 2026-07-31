package com.bookworm.repository;

import com.bookworm.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    Optional<Tag> findByTagNameIgnoreCase(String tagName);
}

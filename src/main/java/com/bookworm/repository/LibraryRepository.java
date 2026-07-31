package com.bookworm.repository;

import com.bookworm.entity.Library;
import com.bookworm.entity.enums.LibraryAccessType;
import com.bookworm.entity.enums.LibraryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibraryRepository extends JpaRepository<Library, Integer> {

    List<Library> findByUser_UserIdOrderByStartDateDesc(Integer userId);

    Optional<Library> findByUser_UserIdAndProduct_ProductIdAndAccessTypeAndStatus(
            Integer userId, Integer productId, LibraryAccessType accessType, LibraryStatus status);
}

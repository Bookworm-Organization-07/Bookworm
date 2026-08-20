package com.example.Repository;

import com.example.models.LibraryAccessType;
import com.example.models.MyLibrary;
import com.example.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MyLibraryRepository extends JpaRepository<MyLibrary, Integer> {

    @Transactional
    void deleteByUser(User user);

    /**
     * Everything still inside its access window - both RENT and LEND
     * rows. This is what the "My Library" page shows.
     */
    List<MyLibrary> findByUser_UserIdAndEndDateAfter(Integer userId, LocalDate today);

    /**
     * How many books a library package borrow counts toward. Scoped to
     * LEND only, on purpose: a book the reader separately paid to RENT
     * must never eat into a library package's book limit - they are two
     * unrelated ways of getting a book, and mixing them up would either
     * block a valid borrow or let someone sneak an extra book past the
     * limit.
     */
    int countByUser_UserIdAndAccessTypeAndEndDateAfter(
            Integer userId,
            LibraryAccessType accessType,
            LocalDate today
    );

    /**
     * "Can this reader open this PDF right now?" - true for either a
     * live rental OR a live library borrow, whichever it is.
     */
    boolean existsByUser_UserIdAndProduct_ProductIdAndEndDateAfter(
            Integer userId,
            Integer productId,
            LocalDate today
    );

    /**
     * "Has this reader already borrowed this exact book through a
     * library package?" Scoped to LEND, for the same reason as the count
     * method above.
     */
    boolean existsByUser_UserIdAndProduct_ProductIdAndAccessTypeAndEndDateAfter(
            Integer userId,
            Integer productId,
            LibraryAccessType accessType,
            LocalDate today
    );

    /**
     * Finds a still-live RENT row for this exact book, if one exists, so
     * renting the same book again can extend it instead of creating a
     * second row.
     */
    Optional<MyLibrary> findByUser_UserIdAndProduct_ProductIdAndAccessTypeAndEndDateAfter(
            Integer userId,
            Integer productId,
            LibraryAccessType accessType,
            LocalDate today
    );
}

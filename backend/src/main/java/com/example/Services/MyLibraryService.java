package com.example.Services;

import com.example.dto.MyLibraryItemResponseDto;
import com.example.models.Product;
import com.example.models.User;

import java.util.List;

public interface MyLibraryService {

    List<MyLibraryItemResponseDto> getUserLibrary(Integer userId);

    byte[] readLibraryBook(Integer userId, Integer productId);

    /**
     * Records a paid rental in the reader's library. Called by
     * CheckoutService after a successful RENT checkout - rented books
     * live in My Library, not on My Shelf (only purchases go there).
     */
    void recordRental(User user, Product product, int rentDays);
}

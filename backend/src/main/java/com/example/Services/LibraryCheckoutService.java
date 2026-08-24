package com.example.Services;

import com.example.dto.LibraryCheckoutRequest;
import com.example.models.User;

public interface LibraryCheckoutService {

    void checkout(User user, LibraryCheckoutRequest request);
}

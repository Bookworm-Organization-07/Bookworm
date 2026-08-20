package com.example.Services;

import com.example.Repository.MyShelfRepository;
import com.example.models.MyShelf;
import com.example.models.Product;
import com.example.models.User;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * My Shelf holds only books the reader BOUGHT outright - never rentals,
 * never library borrows. Those live in My Library instead (see
 * MyLibraryService). Because a purchase never expires, there is nothing
 * to extend or clean up here any more: a book either is on the shelf or
 * it isn't.
 */
@Service
public class ShelfService {

    private final MyShelfRepository shelfRepository;

    public ShelfService(MyShelfRepository shelfRepository) {
        this.shelfRepository = shelfRepository;
    }

    public List<MyShelf> getShelfByUser(Integer userId) {
        return shelfRepository.findByUser_UserId(userId);
    }

    /**
     * Called by CheckoutService after a successful BUY. If the reader
     * already owns this book, buying it again simply does nothing extra
     * (the UNIQUE(user_id, product_id) constraint would reject a second
     * row anyway).
     */
    public void addToShelf(User user, Product product) {
        boolean alreadyOwned = shelfRepository
                .existsByUser_UserIdAndProduct_ProductId(user.getUserId(), product.getProductId());

        if (alreadyOwned) {
            return;
        }

        MyShelf shelf = new MyShelf();
        shelf.setUser(user);
        shelf.setProduct(product);
        shelfRepository.save(shelf);
    }

    public void deleteShelfItem(User user, Integer shelfId) {
        MyShelf shelf = shelfRepository.findById(shelfId)
                .orElseThrow(() -> new IllegalArgumentException("Shelf item not found."));

        if (shelf.getUser().getUserId() != user.getUserId()) {
            throw new IllegalArgumentException("Shelf item not found.");
        }
        shelfRepository.delete(shelf);
    }
}

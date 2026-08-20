package com.example.Services;

import com.example.Repository.CartRepository;
import com.example.Repository.LibraryPackagePurchaseItemRepository;
import com.example.Repository.LibraryPackagePurchaseRepository;
import com.example.Repository.MyLibraryRepository;
import com.example.Repository.MyShelfRepository;
import com.example.Repository.TransactionRepository;
import com.example.Repository.UserRepository;
import com.example.dto.AdminUserSummaryDto;
import com.example.models.LibraryPackagePurchase;
import com.example.models.Transaction;
import com.example.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lists readers/admins for the admin Users screen, and deletes an
 * account when asked to.
 *
 * Deleting a user is not just one DELETE statement: cart, my_shelf, and
 * my_library rows all have a NOT NULL foreign key back to the user, so
 * the database would refuse to delete a user who still has any of those
 * - they have to be cleared first. Past transactions are handled
 * differently on purpose: they are the store's own sales/royalty
 * records, not really "the user's" data, so instead of deleting them
 * (which would also wipe out royalty history) this just detaches them
 * by setting their user to null. transactions.user_id allows null for
 * exactly this reason - see database/01_schema.sql.
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final MyShelfRepository myShelfRepository;
    private final MyLibraryRepository myLibraryRepository;
    private final LibraryPackagePurchaseRepository libraryPackagePurchaseRepository;
    private final LibraryPackagePurchaseItemRepository libraryPackagePurchaseItemRepository;
    private final TransactionRepository transactionRepository;

    public AdminUserService(UserRepository userRepository,
                            CartRepository cartRepository,
                            MyShelfRepository myShelfRepository,
                            MyLibraryRepository myLibraryRepository,
                            LibraryPackagePurchaseRepository libraryPackagePurchaseRepository,
                            LibraryPackagePurchaseItemRepository libraryPackagePurchaseItemRepository,
                            TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.myShelfRepository = myShelfRepository;
        this.myLibraryRepository = myLibraryRepository;
        this.libraryPackagePurchaseRepository = libraryPackagePurchaseRepository;
        this.libraryPackagePurchaseItemRepository = libraryPackagePurchaseItemRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<AdminUserSummaryDto> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            AdminUserSummaryDto dto = new AdminUserSummaryDto();
            dto.setUserId(user.getUserId());
            dto.setUserName(user.getUserName());
            dto.setUserEmail(user.getUserEmail());
            dto.setAdmin(user.isAdmin());
            dto.setJoinDate(user.getJoinDate());
            return dto;
        }).toList();
    }

    /**
     * @Transactional so that if anything here fails partway through
     * (e.g. the final delete hits a foreign key nobody thought of), all
     * the cleanup steps before it are rolled back too instead of
     * leaving the user half-deleted.
     */
    @Transactional
    public void deleteUser(int userId, User callingAdmin) {
        if (userId == callingAdmin.getUserId()) {
            throw new IllegalArgumentException("You cannot delete your own account.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        cartRepository.deleteByUser(user);
        myShelfRepository.deleteByUser(user);
        myLibraryRepository.deleteByUser(user);

        List<LibraryPackagePurchase> purchases =
                libraryPackagePurchaseRepository.findByUser_UserIdOrderByPurchaseDateDesc(userId);
        for (LibraryPackagePurchase purchase : purchases) {
            libraryPackagePurchaseItemRepository.deleteByPurchase(purchase);
        }
        libraryPackagePurchaseRepository.deleteAll(purchases);

        List<Transaction> transactions = transactionRepository.findByUser_UserIdOrderByCreatedAtDesc(userId);
        for (Transaction transaction : transactions) {
            transaction.setUser(null);
        }
        transactionRepository.saveAll(transactions);

        userRepository.delete(user);
    }
}

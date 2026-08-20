package com.example.Services;

import com.example.Repository.LibraryPackagePurchaseRepository;
import com.example.Repository.LibraryPackageRepository;
import com.example.Repository.MyLibraryRepository;
import com.example.Repository.ProductBeneficiaryRepository;
import com.example.Repository.ProductRepository;
import com.example.Repository.RoyaltyCalculationRepository;
import com.example.Repository.TransactionItemRepository;
import com.example.Repository.TransactionRepository;
import com.example.dto.LibraryCheckoutRequest;
import com.example.models.Beneficiary;
import com.example.models.LibraryAccessType;
import com.example.models.LibraryPackage;
import com.example.models.LibraryPackagePurchase;
import com.example.models.LibraryPackagePurchaseItem;
import com.example.models.MyLibrary;
import com.example.models.Product;
import com.example.models.ProductBeneficiary;
import com.example.models.RoyaltyCalculation;
import com.example.models.Transaction;
import com.example.models.TransactionItem;
import com.example.models.TransactionStatus;
import com.example.models.TransactionType;
import com.example.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Borrowing from the lending library.
 *
 * A reader buys a package once, then draws books against it until the
 * package's book limit or validity period runs out. Coming back for more
 * books while the package is still live reuses it and costs nothing
 * extra.
 *
 * Royalty here is notional: the package price divided by the number of
 * books it allows gives each borrow a value, and the title's royalty
 * percentage is applied to that.
 */
@Service
public class LibraryCheckoutServiceImpl implements LibraryCheckoutService {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final LibraryPackageRepository libraryPackageRepository;
    private final LibraryPackagePurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final MyLibraryRepository myLibraryRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionItemRepository transactionItemRepository;
    private final RoyaltyCalculationRepository royaltyRepository;
    private final BeneficiaryAssignmentService beneficiaryAssignmentService;
    private final ProductBeneficiaryRepository productBeneficiaryRepository;
    private final LibraryInvoicePdfService libraryInvoicePdfService;
    private final EmailService emailService;

    public LibraryCheckoutServiceImpl(LibraryPackageRepository libraryPackageRepository,
                                      LibraryPackagePurchaseRepository purchaseRepository,
                                      ProductRepository productRepository,
                                      MyLibraryRepository myLibraryRepository,
                                      TransactionRepository transactionRepository,
                                      TransactionItemRepository transactionItemRepository,
                                      RoyaltyCalculationRepository royaltyRepository,
                                      BeneficiaryAssignmentService beneficiaryAssignmentService,
                                      ProductBeneficiaryRepository productBeneficiaryRepository,
                                      LibraryInvoicePdfService libraryInvoicePdfService,
                                      EmailService emailService) {
        this.libraryPackageRepository = libraryPackageRepository;
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.myLibraryRepository = myLibraryRepository;
        this.transactionRepository = transactionRepository;
        this.transactionItemRepository = transactionItemRepository;
        this.royaltyRepository = royaltyRepository;
        this.beneficiaryAssignmentService = beneficiaryAssignmentService;
        this.productBeneficiaryRepository = productBeneficiaryRepository;
        this.libraryInvoicePdfService = libraryInvoicePdfService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void checkout(User user, LibraryCheckoutRequest request) {

        if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
            throw new IllegalArgumentException("Select at least one title to borrow.");
        }

        LibraryPackage libraryPackage =
                libraryPackageRepository.findById(request.getPackageId())
                        .orElseThrow(() -> new IllegalArgumentException("Library package not found."));

        int bookLimit = libraryPackage.getBookLimit();
        if (bookLimit <= 0) {
            throw new IllegalStateException(
                    "The " + libraryPackage.getName() + " package allows no books.");
        }

        List<Product> products = productRepository.findAllById(request.getProductIds());
        if (products.size() != request.getProductIds().size()) {
            throw new IllegalArgumentException("One of the selected titles no longer exists.");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // -------------------------------------------------------------
        // Reuse a live package of the same kind, or sell a new one.
        // Only a new package creates a charge; topping up an existing one
        // must not bill the reader a second time.
        // -------------------------------------------------------------
        LibraryPackagePurchase activePurchase = findActivePurchase(user, libraryPackage, now);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(TransactionType.LEND);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(now);
        transaction.setTotalAmount(
                activePurchase != null ? BigDecimal.ZERO : libraryPackage.getCost());
        transaction = transactionRepository.save(transaction);

        BigDecimal avgBookPrice = libraryPackage.getCost()
                .divide(BigDecimal.valueOf(bookLimit), MONEY_SCALE, ROUNDING);

        LibraryPackagePurchase purchase;
        if (activePurchase != null) {
            purchase = activePurchase;
        } else {
            purchase = new LibraryPackagePurchase();
            purchase.setUser(user);
            purchase.setTransaction(transaction);
            purchase.setLibraryPackage(libraryPackage);
            purchase.setPackagePrice(libraryPackage.getCost());
            purchase.setAllowedBooks(bookLimit);
            purchase.setAvgBookPrice(avgBookPrice);
            purchase.setPurchaseDate(now);
            purchase = purchaseRepository.save(purchase);
        }

        // -------------------------------------------------------------
        // Enforce the book limit across everything already borrowed
        // through a library package. This deliberately does NOT count
        // books the reader separately paid to rent (LibraryAccessType.RENT)
        // - a rental is a different transaction and must never eat into
        // a package's book limit.
        // -------------------------------------------------------------
        int alreadyBorrowed = myLibraryRepository.countByUser_UserIdAndAccessTypeAndEndDateAfter(
                user.getUserId(), LibraryAccessType.LEND, today);

        if (alreadyBorrowed + products.size() > purchase.getAllowedBooks()) {
            throw new IllegalStateException(
                    "That would take you to " + (alreadyBorrowed + products.size())
                            + " books; the " + libraryPackage.getName() + " package allows "
                            + purchase.getAllowedBooks() + ".");
        }

        LocalDate endDate = today.plusDays(libraryPackage.getValidityDays());
        int booksTaken = alreadyBorrowed;

        for (Product product : products) {
            if (!product.isLibrary()) {
                throw new IllegalStateException(
                        product.getProductName() + " is not available in the lending library.");
            }

            boolean alreadyOut = myLibraryRepository
                    .existsByUser_UserIdAndProduct_ProductIdAndAccessTypeAndEndDateAfter(
                            user.getUserId(), product.getProductId(), LibraryAccessType.LEND, today);
            if (alreadyOut) {
                throw new IllegalStateException(
                        product.getProductName() + " is already in your library.");
            }

            booksTaken++;

            BigDecimal royaltyPercent = product.getRoyaltyPercent() != null
                    ? product.getRoyaltyPercent()
                    : BigDecimal.ZERO;

            BigDecimal royaltyAmount = purchase.getAvgBookPrice()
                    .multiply(royaltyPercent)
                    .divide(HUNDRED, MONEY_SCALE, ROUNDING);

            // A transaction item per borrow keeps the royalty trail and
            // the reader's order history consistent with a normal buy.
            TransactionItem item = new TransactionItem();
            item.setTransaction(transaction);
            item.setProduct(product);
            item.setPrice(purchase.getAvgBookPrice());
            item.setQuantity(1);
            item = transactionItemRepository.save(item);

            // Recorded against the purchase so the invoice can list what
            // was actually borrowed.
            LibraryPackagePurchaseItem purchaseItem = new LibraryPackagePurchaseItem();
            purchaseItem.setProduct(product);
            purchaseItem.setRoyaltyPercent(royaltyPercent);
            purchaseItem.setRoyaltyAmount(royaltyAmount);
            purchase.addItem(purchaseItem);

            recordRoyalty(product, item, purchase.getAvgBookPrice(),
                    royaltyPercent, royaltyAmount, today);

            MyLibrary myLibrary = new MyLibrary();
            myLibrary.setUser(user);
            myLibrary.setLibraryPackage(libraryPackage);
            myLibrary.setProduct(product);
            myLibrary.setAccessType(LibraryAccessType.LEND);
            myLibrary.setStartDate(today);
            myLibrary.setEndDate(endDate);
            myLibrary.setBooksAllowed(bookLimit);
            myLibrary.setBooksTaken(booksTaken);
            myLibraryRepository.save(myLibrary);
        }

        purchase = purchaseRepository.save(purchase);

        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction = transactionRepository.save(transaction);

        byte[] invoice =
                libraryInvoicePdfService.generateLibraryInvoice(purchase, purchase.getItems());
        emailService.sendTransactionSuccessEmail(user.getUserEmail(), transaction, invoice);
    }

    /**
     * The reader's most recent purchase of this same package, if it has
     * not yet run out of validity.
     */
    private LibraryPackagePurchase findActivePurchase(User user,
                                                      LibraryPackage libraryPackage,
                                                      LocalDateTime now) {
        List<LibraryPackagePurchase> previous =
                purchaseRepository.findByUser_UserIdOrderByPurchaseDateDesc(user.getUserId());

        if (previous.isEmpty()) {
            return null;
        }

        LibraryPackagePurchase latest = previous.get(0);
        LocalDateTime expiry = latest.getPurchaseDate()
                .plusDays(latest.getLibraryPackage().getValidityDays());

        boolean stillValid = expiry.isAfter(now);
        boolean samePackage = latest.getLibraryPackage()
                .getPackageId().equals(libraryPackage.getPackageId());

        return (stillValid && samePackage) ? latest : null;
    }

    /**
     * Same rule as a normal purchase: a title with no royalty percentage
     * or no configured beneficiaries earns nothing and does not block the
     * borrow.
     */
    private void recordRoyalty(Product product,
                               TransactionItem item,
                               BigDecimal basis,
                               BigDecimal royaltyPercent,
                               BigDecimal royaltyAmount,
                               LocalDate today) {

        if (royaltyPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        List<Beneficiary> beneficiaries =
                beneficiaryAssignmentService.getAssignedBeneficiaries(product);
        if (beneficiaries.isEmpty()) {
            return;
        }

        RoyaltyCalculation royalty = new RoyaltyCalculation();
        royalty.setProduct(product);
        royalty.setTransactionItem(item);
        royalty.setRoyaltyPercent(royaltyPercent);
        royalty.setTotalAmount(basis);
        royalty.setTotalRoyalty(royaltyAmount);
        royalty.setRoycalTranDate(today);
        royalty = royaltyRepository.save(royalty);

        BigDecimal perBeneficiary = royaltyAmount
                .divide(BigDecimal.valueOf(beneficiaries.size()), MONEY_SCALE, ROUNDING);

        for (Beneficiary beneficiary : beneficiaries) {
            ProductBeneficiary share = new ProductBeneficiary();
            share.setBeneficiary(beneficiary);
            share.setProduct(product);
            share.setRoyaltyCalculation(royalty);
            share.setRoyaltyReceived(perBeneficiary);
            productBeneficiaryRepository.save(share);
        }
    }
}

package com.example.Services;

import com.example.Repository.CartRepository;
import com.example.Repository.ProductBeneficiaryRepository;
import com.example.Repository.RoyaltyCalculationRepository;
import com.example.Repository.TransactionItemRepository;
import com.example.Repository.TransactionRepository;
import com.example.models.Beneficiary;
import com.example.models.Cart;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Turns a cart into one or two paid transactions.
 *
 * Each cart line carries its own buy-or-rent choice (Cart.rentDays -
 * null means buy, a value means rent for that many days), so a single
 * cart can hold both at once. Checkout splits the cart by that choice
 * and produces a separate Transaction per type actually present - a
 * cart of only purchases still produces exactly one Transaction, same
 * as before. Each line produces a TransactionItem, and where the title
 * carries a royalty percentage, a RoyaltyCalculation split across that
 * title's beneficiaries.
 *
 * A BUY line lands on the reader's Shelf (kept forever). A RENT line
 * lands in the reader's Library instead (access until the rented days
 * run out) - see the "buy vs rent" branch inside processTransaction.
 */
@Service
public class CheckoutService {

    /** Money is rounded to paise at every step. */
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final CartRepository cartRepo;
    private final TransactionRepository transactionRepo;
    private final TransactionItemRepository itemRepo;
    private final BeneficiaryAssignmentService beneficiaryAssignmentService;
    private final RoyaltyCalculationRepository royaltyRepo;
    private final ProductBeneficiaryRepository productBeneficiaryRepo;
    private final ShelfService shelfService;
    private final MyLibraryService myLibraryService;
    private final TransactionPdfService transactionPdfService;
    private final EmailService emailService;

    public CheckoutService(CartRepository cartRepo,
                           TransactionRepository transactionRepo,
                           TransactionItemRepository itemRepo,
                           BeneficiaryAssignmentService beneficiaryAssignmentService,
                           RoyaltyCalculationRepository royaltyRepo,
                           ProductBeneficiaryRepository productBeneficiaryRepo,
                           ShelfService shelfService,
                           MyLibraryService myLibraryService,
                           TransactionPdfService transactionPdfService,
                           EmailService emailService) {
        this.cartRepo = cartRepo;
        this.transactionRepo = transactionRepo;
        this.itemRepo = itemRepo;
        this.beneficiaryAssignmentService = beneficiaryAssignmentService;
        this.royaltyRepo = royaltyRepo;
        this.productBeneficiaryRepo = productBeneficiaryRepo;
        this.shelfService = shelfService;
        this.myLibraryService = myLibraryService;
        this.transactionPdfService = transactionPdfService;
        this.emailService = emailService;
    }

    @Transactional
    public List<Transaction> checkout(User user) {

        List<Cart> cartItems = cartRepo.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Your cart is empty.");
        }

        List<Cart> buyLines = cartItems.stream().filter(c -> c.getRentDays() == null).toList();
        List<Cart> rentLines = cartItems.stream().filter(c -> c.getRentDays() != null).toList();

        List<Transaction> transactions = new ArrayList<>();
        if (!buyLines.isEmpty()) {
            transactions.add(processTransaction(user, buyLines, TransactionType.BUY));
        }
        if (!rentLines.isEmpty()) {
            transactions.add(processTransaction(user, rentLines, TransactionType.RENT));
        }

        cartRepo.deleteByUser(user);

        return transactions;
    }

    /** Everything one Transaction needs, for one homogeneous slice (all-buy or all-rent) of the cart. */
    private Transaction processTransaction(User user, List<Cart> cartLines, TransactionType transactionType) {

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTransactionType(transactionType);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction = transactionRepo.save(transaction);

        LocalDate today = LocalDate.now();
        BigDecimal total = BigDecimal.ZERO;

        for (Cart cart : cartLines) {
            Product product = cart.getProduct();
            int qty = cart.getQty();
            Integer rentDays = cart.getRentDays();

            BigDecimal unitPrice = unitPrice(product, transactionType, rentDays);
            BigDecimal lineTotal = unitPrice
                    .multiply(BigDecimal.valueOf(qty))
                    .setScale(MONEY_SCALE, ROUNDING);

            TransactionItem item = new TransactionItem();
            item.setTransaction(transaction);
            item.setProduct(product);
            item.setPrice(unitPrice);
            item.setQuantity(qty);
            item = itemRepo.save(item);

            total = total.add(lineTotal);

            recordRoyalty(product, item, lineTotal, today);

            // Buying puts the book on the Shelf forever; renting puts it
            // in the Library until the rented days run out. The
            // product's own is_rentable flag does not decide which one
            // happens here - what the reader actually paid for does.
            if (transactionType == TransactionType.BUY) {
                shelfService.addToShelf(user, product);
            } else {
                myLibraryService.recordRental(user, product, rentDays);
            }
        }

        transaction.setTotalAmount(total);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction = transactionRepo.save(transaction);

        List<TransactionItem> items = itemRepo.findByTransaction(transaction);
        byte[] invoice = transactionPdfService.generateInvoice(transaction, items);
        emailService.sendTransactionSuccessEmail(user.getUserEmail(), transaction, invoice);

        return transaction;
    }

    /**
     * Price of one copy.
     *
     * Rent: rent_per_day x days.
     * Buy : a live offer price wins; failing that a discount percentage
     *       off the base price; failing that the base price.
     */
    private BigDecimal unitPrice(Product product, TransactionType type, Integer rentDays) {

        if (type == TransactionType.RENT) {
            if (!product.isRentable()) {
                throw new IllegalStateException(
                        product.getProductName() + " is not available to rent.");
            }
            if (product.getRentPerDay() == null) {
                throw new IllegalStateException(
                        "No daily rate is set for " + product.getProductName() + ".");
            }
            if (rentDays < product.getMinRentDays()) {
                throw new IllegalArgumentException(
                        product.getProductName() + " must be rented for at least "
                                + product.getMinRentDays() + " days.");
            }
            return product.getRentPerDay()
                    .multiply(BigDecimal.valueOf(rentDays))
                    .setScale(MONEY_SCALE, ROUNDING);
        }

        boolean offerIsLive =
                product.getProductOfferprice() != null
                        && product.getProductOffPriceExpirydate() != null
                        && product.getProductOffPriceExpirydate().isAfter(LocalDate.now());

        if (offerIsLive) {
            return product.getProductOfferprice().setScale(MONEY_SCALE, ROUNDING);
        }

        boolean hasDiscount =
                product.getDiscountPercent() != null
                        && product.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0;

        if (hasDiscount) {
            BigDecimal discount = product.getProductBaseprice()
                    .multiply(product.getDiscountPercent())
                    .divide(HUNDRED, MONEY_SCALE, ROUNDING);
            return product.getProductBaseprice()
                    .subtract(discount)
                    .setScale(MONEY_SCALE, ROUNDING);
        }

        return product.getProductBaseprice().setScale(MONEY_SCALE, ROUNDING);
    }

    /**
     * Records what this line earned and splits it evenly across the
     * title's beneficiaries. A title with no royalty percentage set
     * simply earns nothing - it does not fail the checkout. One with a
     * royalty percentage but nobody explicitly assigned still gets a
     * beneficiary here (see BeneficiaryAssignmentService), defaulting
     * to the Author - so this only ever skips on the "no percentage"
     * case, not "nobody to pay."
     */
    private void recordRoyalty(Product product,
                               TransactionItem item,
                               BigDecimal lineTotal,
                               LocalDate today) {

        BigDecimal royaltyPercent = product.getRoyaltyPercent();
        if (royaltyPercent == null || royaltyPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        List<Beneficiary> beneficiaries = beneficiaryAssignmentService.getAssignedBeneficiaries(product);
        if (beneficiaries.isEmpty()) {
            return;
        }

        // Scale and rounding are mandatory here: without them a division
        // such as 399.00 x 10 / 100 that does not terminate throws
        // ArithmeticException and takes the whole checkout with it.
        BigDecimal totalRoyalty = lineTotal
                .multiply(royaltyPercent)
                .divide(HUNDRED, MONEY_SCALE, ROUNDING);

        RoyaltyCalculation royalty = new RoyaltyCalculation();
        royalty.setProduct(product);
        royalty.setTransactionItem(item);
        royalty.setRoyaltyPercent(royaltyPercent);
        royalty.setTotalAmount(lineTotal);
        royalty.setTotalRoyalty(totalRoyalty);
        royalty.setRoycalTranDate(today);
        royalty = royaltyRepo.save(royalty);

        BigDecimal perBeneficiary = totalRoyalty
                .divide(BigDecimal.valueOf(beneficiaries.size()), MONEY_SCALE, ROUNDING);

        for (Beneficiary beneficiary : beneficiaries) {
            ProductBeneficiary share = new ProductBeneficiary();
            share.setProduct(product);
            share.setBeneficiary(beneficiary);
            share.setRoyaltyCalculation(royalty);
            share.setRoyaltyReceived(perBeneficiary);
            productBeneficiaryRepo.save(share);
        }
    }
}

package com.example.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A paid-for library package. Books borrowed against it are recorded
 * both as LibraryPackagePurchaseItem rows (for the invoice and the
 * royalty trail) and as MyLibrary rows (for the reader's shelf).
 */
@Entity
@Table(name = "library_package_purchase")
public class LibraryPackagePurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Integer purchaseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private LibraryPackage libraryPackage;

    @Column(name = "package_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal packagePrice;

    @Column(name = "allowed_books", nullable = false)
    private Integer allowedBooks;

    /** package_price / allowed_books - the notional value of one borrow. */
    @Column(name = "avg_book_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal avgBookPrice;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LibraryPackagePurchaseItem> items = new ArrayList<>();

    public Integer getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(Integer purchaseId) {
        this.purchaseId = purchaseId;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LibraryPackage getLibraryPackage() {
        return libraryPackage;
    }

    public void setLibraryPackage(LibraryPackage libraryPackage) {
        this.libraryPackage = libraryPackage;
    }

    public BigDecimal getPackagePrice() {
        return packagePrice;
    }

    public void setPackagePrice(BigDecimal packagePrice) {
        this.packagePrice = packagePrice;
    }

    public Integer getAllowedBooks() {
        return allowedBooks;
    }

    public void setAllowedBooks(Integer allowedBooks) {
        this.allowedBooks = allowedBooks;
    }

    public BigDecimal getAvgBookPrice() {
        return avgBookPrice;
    }

    public void setAvgBookPrice(BigDecimal avgBookPrice) {
        this.avgBookPrice = avgBookPrice;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public List<LibraryPackagePurchaseItem> getItems() {
        return items;
    }

    /** Keeps both sides of the relationship consistent. */
    public void addItem(LibraryPackagePurchaseItem item) {
        item.setPurchase(this);
        this.items.add(item);
    }
}

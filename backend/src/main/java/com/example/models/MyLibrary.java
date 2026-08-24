package com.example.models;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * One row = one book the reader currently has temporary access to,
 * valid until end_date. This covers TWO different situations, told
 * apart by accessType:
 *
 * RENT - the reader paid rent_per_day x days for this one book
 *        (CheckoutService.checkout). There is no libraryPackage on
 *        these rows - packageId, booksAllowed and booksTaken are all
 *        left null, because they simply do not apply to a plain rental.
 *
 * LEND - the reader borrowed this book using a library package they
 *        already own (LibraryCheckoutServiceImpl.checkout). Every field
 *        is filled in for these rows.
 */
@Entity
@Table(name = "my_library")
public class MyLibrary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "my_lib_id")
    private Integer myLibId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Null for a RENT row - a plain rental has no library package. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private LibraryPackage libraryPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 10)
    private LibraryAccessType accessType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    /** Only meaningful for a LEND row - null for a plain rental. */
    @Column(name = "books_allowed")
    private Integer booksAllowed;

    /** Only meaningful for a LEND row - null for a plain rental. */
    @Column(name = "books_taken")
    private Integer booksTaken;

    public MyLibrary() {
    }

    public Integer getMyLibId() {
        return myLibId;
    }

    public void setMyLibId(Integer myLibId) {
        this.myLibId = myLibId;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public LibraryAccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(LibraryAccessType accessType) {
        this.accessType = accessType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getBooksAllowed() {
        return booksAllowed;
    }

    public void setBooksAllowed(Integer booksAllowed) {
        this.booksAllowed = booksAllowed;
    }

    public Integer getBooksTaken() {
        return booksTaken;
    }

    public void setBooksTaken(Integer booksTaken) {
        this.booksTaken = booksTaken;
    }
}

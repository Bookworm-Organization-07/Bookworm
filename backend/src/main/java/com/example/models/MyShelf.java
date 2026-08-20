package com.example.models;

import jakarta.persistence.*;

/**
 * My Shelf holds only books the reader BOUGHT outright. It never expires
 * - that is the whole point of a shelf versus a library. Rented and
 * library-borrowed books live in MyLibrary instead (see that class).
 */
@Entity
@Table(
    name = "my_shelf",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_shelf_user_product",
        columnNames = {"user_id", "product_id"}
    )
)
public class MyShelf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shelf_id")
    private int shelfId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public MyShelf() {
    }

    public int getShelfId() {
        return shelfId;
    }

    public void setShelfId(int shelfId) {
        this.shelfId = shelfId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}

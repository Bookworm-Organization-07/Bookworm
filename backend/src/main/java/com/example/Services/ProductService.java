package com.example.Services;

import com.example.models.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    Product saveProduct(Product product);

    Product getProductById(Integer id);

    List<Product> getAllProducts();

    Product updateProduct(Integer id, Product product);

    /**
     * Changes only the royalty percentage, on purpose - updateProduct
     * above copies isRentable()/isLibrary() unconditionally (they are
     * primitive booleans, not Boolean objects, so there is no way to
     * tell "leave this alone" apart from "set it to false" in that
     * method). A caller that only means to change the royalty rate must
     * not risk silently switching a title's rentable/library flags off.
     */
    Product updateRoyaltyPercent(Integer id, BigDecimal royaltyPercent);

    void deleteProduct(Integer id);

    List<Product> searchProductsByName(String name, Integer limit);

    /** Titles that can be borrowed with a library package. */
    List<Product> getLibraryProducts();

    /** Library-eligible titles, narrowed by genre and/or language. */
    List<Product> filterLibraryProducts(String genere, String language);

    /** The whole catalogue, narrowed by genre and/or language. */
    List<Product> filterProducts(String genere, String language);
}

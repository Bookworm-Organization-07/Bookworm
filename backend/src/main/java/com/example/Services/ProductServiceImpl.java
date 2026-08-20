package com.example.Services;

import com.example.Repository.BeneficiaryAssignmentRepository;
import com.example.Repository.CartRepository;
import com.example.Repository.ProductAttributeRepository;
import com.example.Repository.ProductCoverRepository;
import com.example.Repository.ProductRepository;
import com.example.Repository.ReadBookRepository;
import com.example.models.Product;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private static final int DEFAULT_SEARCH_LIMIT = 10;
    private static final int MAX_SEARCH_LIMIT = 50;

    private final ProductRepository productRepository;
    private final ProductCoverRepository productCoverRepository;
    private final ReadBookRepository readBookRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final BeneficiaryAssignmentRepository beneficiaryAssignmentRepository;
    private final CartRepository cartRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductCoverRepository productCoverRepository,
                              ReadBookRepository readBookRepository,
                              ProductAttributeRepository productAttributeRepository,
                              BeneficiaryAssignmentRepository beneficiaryAssignmentRepository,
                              CartRepository cartRepository) {
        this.productRepository = productRepository;
        this.productCoverRepository = productCoverRepository;
        this.readBookRepository = readBookRepository;
        this.productAttributeRepository = productAttributeRepository;
        this.beneficiaryAssignmentRepository = beneficiaryAssignmentRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product getProductById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Partial update: only the fields present in the request body are
     * applied. is_rentable and is_library are the exception - they are
     * primitives, so an absent value is indistinguishable from false and
     * both flags are always written.
     */
    @Override
    public Product updateProduct(Integer id, Product product) {
        Product existing = getProductById(id);

        // -------- Basic info --------
        if (product.getProductName() != null)
            existing.setProductName(product.getProductName());
        if (product.getProductDescriptionShort() != null)
            existing.setProductDescriptionShort(product.getProductDescriptionShort());
        if (product.getProductDescriptionLong() != null)
            existing.setProductDescriptionLong(product.getProductDescriptionLong());
        if (product.getProductImage() != null)
            existing.setProductImage(product.getProductImage());
        if (product.getProductIsbn() != null)
            existing.setProductIsbn(product.getProductIsbn());

        // -------- Pricing --------
        if (product.getProductBaseprice() != null)
            existing.setProductBaseprice(product.getProductBaseprice());
        if (product.getProductOfferprice() != null)
            existing.setProductOfferprice(product.getProductOfferprice());
        if (product.getDiscountPercent() != null)
            existing.setDiscountPercent(product.getDiscountPercent());
        if (product.getRoyaltyPercent() != null)
            existing.setRoyaltyPercent(product.getRoyaltyPercent());
        if (product.getProductOffPriceExpirydate() != null)
            existing.setProductOffPriceExpirydate(product.getProductOffPriceExpirydate());

        // -------- Relationships --------
        if (product.getProductType() != null)
            existing.setProductType(product.getProductType());
        if (product.getAuthor() != null)
            existing.setAuthor(product.getAuthor());
        if (product.getPublisher() != null)
            existing.setPublisher(product.getPublisher());
        if (product.getLanguage() != null)
            existing.setLanguage(product.getLanguage());
        if (product.getGenere() != null)
            existing.setGenere(product.getGenere());

        // -------- Rental / library --------
        existing.setRentable(product.isRentable());
        existing.setLibrary(product.isLibrary());
        if (product.getRentPerDay() != null)
            existing.setRentPerDay(product.getRentPerDay());
        if (product.getMinRentDays() > 0)
            existing.setMinRentDays(product.getMinRentDays());

        return productRepository.save(existing);
    }

    @Override
    public Product updateRoyaltyPercent(Integer id, BigDecimal royaltyPercent) {
        if (royaltyPercent == null) {
            throw new IllegalArgumentException("royaltyPercent is required.");
        }
        Product existing = getProductById(id);
        existing.setRoyaltyPercent(royaltyPercent);
        return productRepository.save(existing);
    }

    /**
     * A product's cover, ebook file, spreadsheet-imported attributes,
     * configured royalty payees, and anyone's still-pending cart line
     * are all incidental to the product itself - deleting the product
     * makes them meaningless too, so they are cleared first to satisfy
     * their foreign keys.
     *
     * Anything left referencing the product after that is real history
     * (a sale, a rental, a royalty already paid out, a reader's shelf
     * or library access) rather than something safe to guess about, so
     * it is never silently cascaded away - the whole attempt rolls back
     * and the caller gets a clear "can't delete" message instead of a
     * raw database error.
     */
    @Override
    @Transactional
    public void deleteProduct(Integer id) {
        Product product = getProductById(id);

        try {
            productCoverRepository.findByProduct_ProductId(id).ifPresent(productCoverRepository::delete);
            readBookRepository.findByProduct_ProductId(id).ifPresent(readBookRepository::delete);
            productAttributeRepository.deleteByProduct_ProductId(id);
            beneficiaryAssignmentRepository.deleteByProduct_ProductId(id);
            cartRepository.deleteByProduct_ProductId(id);

            productRepository.delete(product);
            productRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                    "Can't delete '" + product.getProductName()
                            + "' - it has already been sold, rented, or borrowed.");
        }
    }

    @Override
    public List<Product> searchProductsByName(String name, Integer limit) {
        String query = (name == null) ? "" : name.trim();

        // An empty search matches nothing rather than everything, so a
        // cleared search box does not pull the whole catalogue.
        if (query.isEmpty()) {
            return List.of();
        }

        int safeLimit = (limit == null)
                ? DEFAULT_SEARCH_LIMIT
                : Math.min(Math.max(limit, 1), MAX_SEARCH_LIMIT);

        return productRepository.findByProductNameContainingIgnoreCaseOrProductNameEnglishContainingIgnoreCase(
                query, query, PageRequest.of(0, safeLimit));
    }

    @Override
    public List<Product> getLibraryProducts() {
        return productRepository.findByLibraryTrue();
    }

    @Override
    public List<Product> filterLibraryProducts(String genere, String language) {
        boolean hasGenere = isSet(genere);
        boolean hasLanguage = isSet(language);

        if (hasGenere && hasLanguage) {
            return productRepository
                    .findByGenere_GenereDescAndLanguage_LanguageDescAndLibraryTrue(genere, language);
        }
        if (hasGenere) {
            return productRepository.findByGenere_GenereDescAndLibraryTrue(genere);
        }
        if (hasLanguage) {
            return productRepository.findByLanguage_LanguageDescAndLibraryTrue(language);
        }
        return productRepository.findByLibraryTrue();
    }

    @Override
    public List<Product> filterProducts(String genere, String language) {
        boolean hasGenere = isSet(genere);
        boolean hasLanguage = isSet(language);

        if (hasGenere && hasLanguage) {
            return productRepository
                    .findByGenere_GenereDescAndLanguage_LanguageDesc(genere, language);
        }
        if (hasGenere) {
            return productRepository.findByGenere_GenereDesc(genere);
        }
        if (hasLanguage) {
            return productRepository.findByLanguage_LanguageDesc(language);
        }
        return productRepository.findAll();
    }

    private boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}

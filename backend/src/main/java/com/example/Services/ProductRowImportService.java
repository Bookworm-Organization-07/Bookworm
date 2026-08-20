package com.example.Services;

import com.example.Repository.AuthorRepository;
import com.example.Repository.GenereRepository;
import com.example.Repository.LanguageRepository;
import com.example.Repository.ProductRepository;
import com.example.Repository.ProductTypeRepository;
import com.example.Repository.PublisherRepository;
import com.example.models.Author;
import com.example.models.Genere;
import com.example.models.Language;
import com.example.models.Product;
import com.example.models.Product_Type;
import com.example.models.Publisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Turns ONE parsed spreadsheet row into a Product row (or decides to
 * skip it), reusing existing Author/Publisher/Language/Genere rows where
 * possible instead of creating duplicates.
 *
 * @Transactional(REQUIRES_NEW) means every row gets its OWN database
 * transaction. If row 12 fails, only row 12's half-finished work is
 * undone - rows 1 through 11 that already succeeded stay saved. Without
 * REQUIRES_NEW, one bad row partway through a big file would undo the
 * whole batch.
 */
@Service
public class ProductRowImportService {

    /**
     * The spreadsheet writes languages in the native script (मराठी,
     * हिंदी, कोंकणी). Our own `language` table already has English rows
     * for these (see database/02_seed.sql) - so every native label is
     * translated to the matching English one here. Without this, every
     * Marathi book would create a brand-new "मराठी" language row instead
     * of reusing the existing "Marathi" one, and the Language filter
     * dropdown would show duplicates of the same language.
     */
    private static final Map<String, String> LANGUAGE_TRANSLATION = Map.of(
            "मराठी", "Marathi",
            "हिंदी", "Hindi",
            "कोंकणी", "Konkani",
            "english", "English"
    );

    /**
     * The spreadsheet's product-type labels ("e-Book") are close to, but
     * not exactly, the ones already seeded in product_type_master
     * ("eBook") - see database/02_seed.sql. Translating them here means
     * an uploaded eBook lands in the same type as our hand-seeded ones
     * instead of creating a near-duplicate "e-Book" type next to it.
     */
    private static final Map<String, String> PRODUCT_TYPE_TRANSLATION = Map.of(
            "e-book", "eBook",
            "e-audio books", "Audiobook",
            "e-comics", "Comics"
    );

    /** No rent-price column exists in the source file, so a rentable title needs a made-up daily rate. */
    private static final BigDecimal RENT_PRICE_PERCENT_OF_SALE = new BigDecimal("0.10");
    private static final int DEFAULT_MIN_RENT_DAYS = 1;

    /** How far out a bulk-imported special_price is treated as "on offer" (see applySpecialPrice below). */
    private static final long OFFER_VALID_YEARS = 10;

    private final ProductRepository productRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final GenereRepository genereRepository;
    private final LanguageRepository languageRepository;
    private final ProductTypeRepository productTypeRepository;

    public ProductRowImportService(ProductRepository productRepository,
                                   AuthorRepository authorRepository,
                                   PublisherRepository publisherRepository,
                                   GenereRepository genereRepository,
                                   LanguageRepository languageRepository,
                                   ProductTypeRepository productTypeRepository) {
        this.productRepository = productRepository;
        this.authorRepository = authorRepository;
        this.publisherRepository = publisherRepository;
        this.genereRepository = genereRepository;
        this.languageRepository = languageRepository;
        this.productTypeRepository = productTypeRepository;
    }

    public enum Outcome {
        CREATED, SKIPPED
    }

    /** productId is null for a SKIPPED row - nothing was created, so there is nothing a cover could attach to. */
    public record RowResult(Outcome outcome, String message, Integer productId) {
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RowResult importRow(ExcelProductImportService.ParsedProductRow row) {

        // Library packages (e.g. "Granthalay 30 Days Package") are a
        // different kind of row entirely - they belong on the Lending
        // Library admin screen, not here.
        boolean isPackageRow = "Package".equalsIgnoreCase(row.attributeSet())
                || "Yes".equalsIgnoreCase(row.isPackage());
        if (isPackageRow) {
            return new RowResult(Outcome.SKIPPED,
                    "Skipped - '" + row.prodName() + "' is a library package, not a book. "
                            + "Add packages from Admin > Products instead.",
                    null);
        }

        Optional<Product> existingProduct = productRepository.findByProductNameIgnoreCase(row.prodName());
        if (existingProduct.isPresent()) {
            // productId is still passed back here (unlike the package
            // case above) - the row itself is not re-created, but a
            // matching cover image is still worth attaching to the book
            // that is already there (see ExcelProductImportService.
            // findBestCoverMatch). This is what lets an admin re-upload
            // the same spreadsheet and cover folder later to backfill
            // covers onto books that were imported before covers were
            // part of this feature.
            return new RowResult(Outcome.SKIPPED,
                    "Skipped - '" + row.prodName() + "' is already in the catalogue.",
                    existingProduct.get().getProductId());
        }

        if (row.price() == null) {
            throw new IllegalArgumentException("No price given for '" + row.prodName() + "'");
        }

        String[] segments = splitTypeLanguageCategory(row.typeLanguageCategory());

        Product product = new Product();
        product.setProductName(truncate(row.prodName(), 150));
        product.setProductNameEnglish(truncate(row.nameEnglish(), 150));
        product.setProductType(resolveProductType(segments.length > 0 ? segments[0] : "eBook"));
        product.setLanguage(resolveLanguage(segments.length > 1 ? segments[1] : "English"));
        product.setGenere(resolveGenere(segments.length > 2 ? segments[2] : "General"));

        if (row.author() != null && !"package".equalsIgnoreCase(row.author())) {
            product.setAuthor(resolveAuthor(row.author()));
        }
        if (row.publisher() != null) {
            product.setPublisher(resolvePublisher(row.publisher()));
        }

        product.setProductBaseprice(row.price());
        applySpecialPrice(product, row);

        product.setProductDescriptionLong(stripHtml(row.description()));
        product.setProductDescriptionShort(truncate(stripHtml(row.shortDescription()), 255));

        // The source spreadsheet has no ISBN column at all, but our
        // schema requires one (and it must be unique). This placeholder
        // makes that constraint happy; an admin can replace it with the
        // book's real ISBN later if it matters. product_isbn is only
        // VARCHAR(20), so the placeholder has to stay short: "BU-" (3
        // chars) plus 12 hex characters from a random UUID is 15,
        // comfortably under the limit with room to spare.
        product.setProductIsbn("BU-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());

        applyAvailability(product, row.availability());

        // No royalty data exists in this spreadsheet at all - not even
        // who the beneficiary would be, let alone their bank details -
        // so royalty is deliberately left unset. An admin can add it
        // (and a matching beneficiary) later if this title needs one.

        product = productRepository.save(product);

        return new RowResult(Outcome.CREATED,
                "Created product #" + product.getProductId() + " '" + row.prodName() + "'",
                product.getProductId());
    }

    // =================================================================
    // "Type/Language/category" splitting
    // =================================================================

    /** e.g. "e-Book/मराठी/कथा" -> ["e-Book", "मराठी", "कथा"] */
    private String[] splitTypeLanguageCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            return new String[0];
        }
        String[] parts = raw.split("/");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

    // =================================================================
    // Find-or-create lookups
    // =================================================================

    private Product_Type resolveProductType(String rawType) {
        String translated = PRODUCT_TYPE_TRANSLATION.getOrDefault(rawType.trim().toLowerCase(), rawType.trim());
        return productTypeRepository.findByTypeDescIgnoreCase(translated)
                .orElseGet(() -> {
                    Product_Type created = new Product_Type();
                    created.setTypeDesc(truncate(translated, 50));
                    return productTypeRepository.save(created);
                });
    }

    private Language resolveLanguage(String rawLanguage) {
        String translated = LANGUAGE_TRANSLATION.getOrDefault(rawLanguage.trim().toLowerCase(), rawLanguage.trim());
        return languageRepository.findByLanguageDescIgnoreCase(translated)
                .orElseGet(() -> {
                    Language created = new Language();
                    created.setLanguageDesc(truncate(translated, 50));
                    return languageRepository.save(created);
                });
    }

    private Genere resolveGenere(String rawGenre) {
        return genereRepository.findByGenereDescIgnoreCase(rawGenre)
                .orElseGet(() -> {
                    Genere created = new Genere();
                    created.setGenereDesc(truncate(rawGenre, 50));
                    return genereRepository.save(created);
                });
    }

    private Author resolveAuthor(String rawName) {
        String name = rawName.trim();
        return authorRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Author created = new Author();
                    created.setName(truncate(name, 100));
                    return authorRepository.save(created);
                });
    }

    /**
     * The Publisher table requires a unique email, but the spreadsheet
     * has no publisher email column at all. A placeholder, derived from
     * the publisher's name, is generated only the first time that
     * publisher is seen - every book from the same publisher afterward
     * reuses the same row. An admin can replace the placeholder with a
     * real email later if payouts ever need one.
     */
    private Publisher resolvePublisher(String rawName) {
        String name = rawName.trim();
        return publisherRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    Publisher created = new Publisher();
                    created.setName(truncate(name, 100));
                    created.setEmail("bulk-upload+" + UUID.randomUUID() + "@placeholder.local");
                    return publisherRepository.save(created);
                });
    }

    // =================================================================
    // Pricing and availability
    // =================================================================

    /**
     * special_price becomes the product's offer price. There is no
     * expiry date for it in the source file, so it is given a very long
     * one (10 years out) - in effect "on offer until an admin changes
     * it" - matching CheckoutService's rule that an offer only applies
     * while its expiry date is still in the future.
     */
    private void applySpecialPrice(Product product, ExcelProductImportService.ParsedProductRow row) {
        if (row.specialPrice() == null || row.specialPrice().compareTo(row.price()) >= 0) {
            return; // no special price, or it isn't actually cheaper - leave the base price as-is
        }
        product.setProductOfferprice(row.specialPrice());
        product.setProductOffPriceExpirydate(LocalDate.now().plusYears(OFFER_VALID_YEARS));
    }

    /**
     * The source file has one "availability" column instead of our two
     * separate flags - e.g. "Sale,Rent,Package" means sellable AND
     * rentable AND lendable through a library package. This reads that
     * one column and sets both of our flags from it.
     */
    private void applyAvailability(Product product, String availability) {
        String text = availability == null ? "" : availability.toLowerCase();

        boolean rentable = text.contains("rent");
        boolean lendable = text.contains("package");

        product.setRentable(rentable);
        product.setLibrary(lendable);

        if (rentable) {
            BigDecimal basis = product.getProductOfferprice() != null
                    ? product.getProductOfferprice()
                    : product.getProductBaseprice();
            product.setRentPerDay(basis.multiply(RENT_PRICE_PERCENT_OF_SALE).setScale(2, RoundingMode.HALF_UP));
            product.setMinRentDays(DEFAULT_MIN_RENT_DAYS);
        }
    }

    // =================================================================
    // Small helpers
    // =================================================================

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    /**
     * The description columns are HTML (the source is a Magento
     * export), but this project's frontend does not render HTML - it
     * would show the reader literal "<p>" tags. This converts the
     * common tags and entities in this file into plain text. It is not
     * a full HTML parser, just enough for what this file actually
     * contains.
     */
    private String stripHtml(String html) {
        if (html == null) {
            return null;
        }
        String withBreaks = html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n\n")
                .replaceAll("(?i)<[^>]+>", "");
        String unescaped = withBreaks
                .replace("&nbsp;", " ")
                .replace("&ldquo;", "“")
                .replace("&rdquo;", "”")
                .replace("&lsquo;", "‘")
                .replace("&rsquo;", "’")
                .replace("&mdash;", "—")
                .replace("&ndash;", "–")
                .replace("&hellip;", "…")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
        return unescaped.replaceAll("[ \\t]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
    }
}

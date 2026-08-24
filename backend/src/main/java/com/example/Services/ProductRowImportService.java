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

@Service
public class ProductRowImportService {

   
    private static final Map<String, String> LANGUAGE_TRANSLATION = Map.of(
            "मराठी", "Marathi",
            "हिंदी", "Hindi",
            "कोंकणी", "Konkani",
            "english", "English"
    );

    private static final Map<String, String> PRODUCT_TYPE_TRANSLATION = Map.of(
            "e-book", "eBook",
            "e-audio books", "Audiobook",
            "e-comics", "Comics"
    );


    private static final BigDecimal RENT_PRICE_PERCENT_OF_SALE = new BigDecimal("0.10");
    private static final int DEFAULT_MIN_RENT_DAYS = 1;


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


    public record RowResult(Outcome outcome, String message, Integer productId) {
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RowResult importRow(ExcelProductImportService.ParsedProductRow row) {

        
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

       
        product.setProductIsbn("BU-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());

        applyAvailability(product, row.availability());

    

        product = productRepository.save(product);

        return new RowResult(Outcome.CREATED,
                "Created product #" + product.getProductId() + " '" + row.prodName() + "'",
                product.getProductId());
    }

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

    private void applySpecialPrice(Product product, ExcelProductImportService.ParsedProductRow row) {
        if (row.specialPrice() == null || row.specialPrice().compareTo(row.price()) >= 0) {
            return; // no special price, or it isn't actually cheaper - leave the base price as-is
        }
        product.setProductOfferprice(row.specialPrice());
        product.setProductOffPriceExpirydate(LocalDate.now().plusYears(OFFER_VALID_YEARS));
    }

    
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

    
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

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

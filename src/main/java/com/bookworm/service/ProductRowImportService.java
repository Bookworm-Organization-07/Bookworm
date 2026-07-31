package com.bookworm.service;

import com.bookworm.entity.*;
import com.bookworm.entity.enums.PayoutMode;
import com.bookworm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Imports a single parsed catalog row within its own transaction, so one bad row
 * (bad category mapping, missing price, etc.) never rolls back the rows around it.
 */
@Service
public class ProductRowImportService {

    /** Native-script language labels as they appear in the source export -> our seeded language_code. */
    private static final Map<String, String> NATIVE_LANGUAGE_TO_CODE = Map.of(
            "मराठी", "mr",
            "हिंदी", "hi",
            "कोंकणी", "kok",
            "english", "en");

    private static final Map<String, String> PRODUCT_TYPE_TO_CATEGORY = Map.of(
            "e-book", "eBook",
            "e-audio books", "Audio-Book",
            "e-comics", "eBook");

    private static final BigDecimal DEFAULT_AUTHOR_ROYALTY_PERCENT = new BigDecimal("40.00");
    private static final BigDecimal DEFAULT_PUBLISHER_ROYALTY_PERCENT = new BigDecimal("10.00");

    private static final Pattern DAYS_PATTERN = Pattern.compile("(\\d+)\\s*days", Pattern.CASE_INSENSITIVE);
    private static final Pattern BOOKS_PATTERN = Pattern.compile("borrow\\s+(\\d+)\\s+books", Pattern.CASE_INSENSITIVE);

    private final ProductCategoryRepository categoryRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final ProductRepository productRepository;
    private final StakeholderRepository stakeholderRepository;
    private final StakeholderRoleRepository stakeholderRoleRepository;
    private final ProductStakeholderRepository productStakeholderRepository;
    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;
    private final LibraryPackageRepository libraryPackageRepository;

    public ProductRowImportService(
            ProductCategoryRepository categoryRepository,
            GenreRepository genreRepository,
            LanguageRepository languageRepository,
            ProductRepository productRepository,
            StakeholderRepository stakeholderRepository,
            StakeholderRoleRepository stakeholderRoleRepository,
            ProductStakeholderRepository productStakeholderRepository,
            TagRepository tagRepository,
            ProductTagRepository productTagRepository,
            LibraryPackageRepository libraryPackageRepository) {
        this.categoryRepository = categoryRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.productRepository = productRepository;
        this.stakeholderRepository = stakeholderRepository;
        this.stakeholderRoleRepository = stakeholderRoleRepository;
        this.productStakeholderRepository = productStakeholderRepository;
        this.tagRepository = tagRepository;
        this.productTagRepository = productTagRepository;
        this.libraryPackageRepository = libraryPackageRepository;
    }

    public enum Outcome {
        CREATED, ALREADY_EXISTS, PACKAGE_UPSERTED
    }

    public record RowResult(Outcome outcome, String message) {
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RowResult importRow(ExcelProductImportService.ParsedProductRow row) {
        boolean isPackageRow = "Package".equalsIgnoreCase(row.attributeSet());
        if (isPackageRow) {
            return upsertLibraryPackage(row);
        }
        return upsertProduct(row);
    }

    private RowResult upsertLibraryPackage(ExcelProductImportService.ParsedProductRow row) {
        String packageName = row.nameEnglish() != null ? row.nameEnglish() : row.prodName();

        Matcher daysMatcher = DAYS_PATTERN.matcher(row.description() == null ? "" : row.description());
        int validDays = daysMatcher.find() ? Integer.parseInt(daysMatcher.group(1)) : 30;

        Matcher booksMatcher = BOOKS_PATTERN.matcher(row.description() == null ? "" : row.description());
        int booksAllowed = booksMatcher.find() ? Integer.parseInt(booksMatcher.group(1)) : 10;

        LibraryPackage pkg = libraryPackageRepository.findByPackageNameIgnoreCase(packageName)
                .orElseGet(LibraryPackage::new);
        pkg.setPackageName(packageName);
        pkg.setPrice(row.price() != null ? row.price() : BigDecimal.ZERO);
        pkg.setValidDays(validDays);
        pkg.setBooksAllowed(booksAllowed);
        pkg.setIsActive(row.status() != null && row.status() == 1);
        libraryPackageRepository.save(pkg);

        return new RowResult(Outcome.PACKAGE_UPSERTED,
                "Library package '" + packageName + "' (" + validDays + " days / " + booksAllowed + " books)");
    }

    private RowResult upsertProduct(ExcelProductImportService.ParsedProductRow row) {
        String[] segments = splitTypeLanguageCategory(row.typeLanguageCategory());
        String productType = segments[0].trim().toLowerCase();

        String categoryName = PRODUCT_TYPE_TO_CATEGORY.get(productType);
        if (categoryName == null) {
            throw new IllegalArgumentException("Unrecognized product type '" + segments[0] + "'");
        }
        ProductCategory category = categoryRepository.findByCategoryNameIgnoreCase(categoryName)
                .orElseThrow(() -> new IllegalStateException("Category not seeded: " + categoryName));

        Product existing = productRepository
                .findByTitleIgnoreCaseAndCategory_CategoryId(row.prodName(), category.getCategoryId())
                .orElse(null);
        if (existing != null) {
            return new RowResult(Outcome.ALREADY_EXISTS,
                    "'" + row.prodName() + "' already exists as product #" + existing.getProductId() + " - skipped");
        }

        Language language = resolveLanguage(segments.length > 1 ? segments[1] : "English");

        String genreName = "e-comics".equals(productType)
                ? "Comics"
                : (segments.length > 2 ? segments[2] : "General");
        Genre genre = findOrCreateGenre(category, genreName);

        BigDecimal basePrice = row.price() != null ? row.price() : BigDecimal.ZERO;
        BigDecimal salePrice = row.specialPrice() != null ? row.specialPrice() : basePrice;

        boolean rentable = row.availability() != null && row.availability().toLowerCase().contains("rent");
        boolean lendable = row.availability() != null && row.availability().toLowerCase().contains("package");

        Product product = Product.builder()
                .category(category)
                .genre(genre)
                .language(language)
                .title(row.prodName())
                .titleEnglish(row.nameEnglish())
                .description(stripHtml(row.description()))
                .basePrice(basePrice)
                .salePrice(salePrice)
                .isRentable(rentable)
                .isLendable(lendable)
                .isFeatured(false)
                .isBestseller(false)
                .isActive(row.status() != null && row.status() == 1)
                .build();
        product = productRepository.save(product);

        if (row.author() != null && !row.author().isBlank() && !"package".equalsIgnoreCase(row.author())) {
            linkStakeholder(product, row.author(), "Author", DEFAULT_AUTHOR_ROYALTY_PERCENT);
        }
        if (row.publisher() != null && !row.publisher().isBlank()) {
            linkStakeholder(product, row.publisher(), "Publisher", DEFAULT_PUBLISHER_ROYALTY_PERCENT);
        }
        if (row.metaKeyword() != null && !row.metaKeyword().isBlank()) {
            linkTags(product, row.metaKeyword());
        }

        return new RowResult(Outcome.CREATED, "Created product #" + product.getProductId() + " '" + row.prodName() + "'");
    }

    private String[] splitTypeLanguageCategory(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Missing 'Type/Language/category' value");
        }
        String[] parts = raw.split("/");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

    private Language resolveLanguage(String nativeLabel) {
        String key = nativeLabel.trim().toLowerCase();
        String code = NATIVE_LANGUAGE_TO_CODE.getOrDefault(key, key);
        return languageRepository.findAll().stream()
                .filter(l -> l.getLanguageCode().equalsIgnoreCase(code) || l.getLanguageName().equalsIgnoreCase(nativeLabel.trim()))
                .findFirst()
                .orElseGet(() -> {
                    Language created = Language.builder()
                            .languageCode(code.length() > 10 ? code.substring(0, 10) : code)
                            .languageName(nativeLabel.trim())
                            .isDefault(false)
                            .build();
                    return languageRepository.save(created);
                });
    }

    private Genre findOrCreateGenre(ProductCategory category, String genreName) {
        return genreRepository.findByCategory_CategoryIdAndGenreNameIgnoreCase(category.getCategoryId(), genreName)
                .orElseGet(() -> genreRepository.save(Genre.builder()
                        .category(category)
                        .genreName(genreName)
                        .displayOrder(0)
                        .build()));
    }

    private void linkStakeholder(Product product, String fullName, String roleName, BigDecimal royaltyPercent) {
        String trimmedName = fullName.trim();
        Stakeholder stakeholder = stakeholderRepository.findByFullNameIgnoreCase(trimmedName)
                .orElseGet(() -> stakeholderRepository.save(Stakeholder.builder()
                        .fullName(trimmedName)
                        .payoutMode(PayoutMode.OTHER)
                        .build()));

        StakeholderRole role = stakeholderRoleRepository.findAll().stream()
                .filter(r -> r.getRoleName().equalsIgnoreCase(roleName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Stakeholder role not seeded: " + roleName));

        productStakeholderRepository.save(ProductStakeholder.builder()
                .product(product)
                .stakeholder(stakeholder)
                .stakeholderRole(role)
                .royaltyPercentage(royaltyPercent)
                .build());
    }

    private void linkTags(Product product, String metaKeyword) {
        Map<String, Tag> seen = new LinkedHashMap<>();
        for (String rawTag : metaKeyword.split(",")) {
            String tagName = rawTag.trim();
            if (tagName.isEmpty() || seen.containsKey(tagName.toLowerCase())) continue;
            Tag tag = tagRepository.findByTagNameIgnoreCase(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().tagName(tagName).build()));
            seen.put(tagName.toLowerCase(), tag);
            productTagRepository.save(ProductTag.builder()
                    .id(new ProductTagId(product.getProductId(), tag.getTagId()))
                    .product(product)
                    .tag(tag)
                    .build());
        }
    }

    /** Minimal, dependency-free HTML-to-plain-text conversion for imported descriptions. */
    private static String stripHtml(String html) {
        if (html == null) return null;
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

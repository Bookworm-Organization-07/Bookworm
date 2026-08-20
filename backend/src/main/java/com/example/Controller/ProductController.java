package com.example.Controller;

import com.example.Services.BeneficiaryAssignmentService;
import com.example.Services.ExcelProductImportService;
import com.example.Services.ProductCoverService;
import com.example.Services.ProductRowImportService;
import com.example.Services.ProductService;
import com.example.dto.BulkUploadResultDto;
import com.example.dto.ManualProductRequest;
import com.example.dto.UpdateRoyaltyRequest;
import com.example.models.Beneficiary;
import com.example.models.Product;
import com.example.models.ProductCover;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * GET is public; POST, PATCH and DELETE are admin-only. The split is
 * enforced by HTTP method in SecurityConfig.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ExcelProductImportService excelProductImportService;
    private final ProductRowImportService productRowImportService;
    private final ProductCoverService productCoverService;
    private final BeneficiaryAssignmentService beneficiaryAssignmentService;

    public ProductController(ProductService productService,
                             ExcelProductImportService excelProductImportService,
                             ProductRowImportService productRowImportService,
                             ProductCoverService productCoverService,
                             BeneficiaryAssignmentService beneficiaryAssignmentService) {
        this.productService = productService;
        this.excelProductImportService = excelProductImportService;
        this.productRowImportService = productRowImportService;
        this.productCoverService = productCoverService;
        this.beneficiaryAssignmentService = beneficiaryAssignmentService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(productService.searchProductsByName(name, limit));
    }

    /** Titles that can be borrowed with a library package. */
    @GetMapping("/library")
    public ResponseEntity<List<Product>> libraryProducts(
            @RequestParam(required = false) String genere,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(productService.filterLibraryProducts(genere, language));
    }

    /** The whole catalogue, optionally narrowed by genre and language. */
    @GetMapping("/filter")
    public ResponseEntity<List<Product>> filterProducts(
            @RequestParam(required = false) String genere,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(productService.filterProducts(genere, language));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return new ResponseEntity<>(productService.saveProduct(product), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Integer id,
                                                 @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /** Changes only the royalty percentage - see ProductService.updateRoyaltyPercent for why this is separate from PATCH /{id}. */
    @PatchMapping("/{id}/royalty")
    public ResponseEntity<Product> updateRoyalty(@PathVariable Integer id,
                                                 @RequestBody UpdateRoyaltyRequest request) {
        return ResponseEntity.ok(productService.updateRoyaltyPercent(id, request.getRoyaltyPercent()));
    }

    /**
     * Who this book's royalty is currently split across. Never empty for
     * a product with an author - see BeneficiaryAssignmentService for
     * the Author-default fallback.
     */
    @GetMapping("/{id}/beneficiaries")
    public ResponseEntity<List<Beneficiary>> getAssignedBeneficiaries(@PathVariable Integer id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(beneficiaryAssignmentService.getAssignedBeneficiaries(product));
    }

    /** Attaches an existing beneficiary (from the pool at GET /api/beneficiaries) to this book. */
    @PostMapping("/{id}/beneficiaries/{beneficiaryId}")
    public ResponseEntity<Void> assignBeneficiary(@PathVariable Integer id, @PathVariable int beneficiaryId) {
        Product product = productService.getProductById(id);
        beneficiaryAssignmentService.assign(product, beneficiaryId);
        return ResponseEntity.noContent().build();
    }

    /** Refuses if this would leave the book with zero beneficiaries - see BeneficiaryAssignmentService.unassign. */
    @DeleteMapping("/{id}/beneficiaries/{beneficiaryId}")
    public ResponseEntity<Void> unassignBeneficiary(@PathVariable Integer id, @PathVariable int beneficiaryId) {
        Product product = productService.getProductById(id);
        beneficiaryAssignmentService.unassign(product, beneficiaryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds one book by hand, without needing a whole spreadsheet. Reuses
     * ProductRowImportService.importRow - the same code the bulk-upload
     * path uses for each spreadsheet row - so a hand-added book gets the
     * exact same treatment a bulk-uploaded one would (a placeholder ISBN,
     * an auto-calculated rent price, and so on). See ManualProductRequest
     * for the field list.
     */
    @PostMapping("/quick-add")
    public ResponseEntity<Map<String, Object>> quickAdd(@RequestBody ManualProductRequest request) {
        String typeLanguageGenre = String.join("/",
                request.getType(), request.getLanguage(), request.getGenre());

        String availability = (request.isRentable() ? "Rent" : "")
                + "," + (request.isLibrary() ? "Package" : "");

        ExcelProductImportService.ParsedProductRow row = new ExcelProductImportService.ParsedProductRow(
                0, // there is no spreadsheet row number here - this book was typed in directly
                request.getProdName(),
                request.getNameEnglish(),
                null, // attributeSet - only used to spot a "library package" row in the bulk file, never true here
                typeLanguageGenre,
                availability,
                request.getAuthor(),
                request.getPublisher(),
                request.getDescription(),
                request.getShortDescription(),
                null, // isPackage - a hand-added book is always a real book, never a package row
                request.getPrice(),
                request.getSpecialPrice(),
                null); // coverId - only meaningful for a bulk-uploaded row matched against a cover folder

        ProductRowImportService.RowResult result = productRowImportService.importRow(row);
        return ResponseEntity.ok(Map.of("message", result.message(), "productId", result.productId()));
    }

    /**
     * Bulk-imports books from an admin-uploaded .xlsx catalogue file
     * (the "Prod Master Table" layout). Admin only, same as every other
     * write on this controller. coverFiles is optional and unordered -
     * each row is matched to its cover by filename (see
     * ExcelProductImportService), not by the order files were selected in.
     */
    @PostMapping("/bulk-upload")
    public ResponseEntity<BulkUploadResultDto> bulkUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "coverFiles", required = false) List<MultipartFile> coverFiles) {
        return ResponseEntity.ok(excelProductImportService.importFromExcel(file, coverFiles));
    }

    /** Uploading again replaces the product's existing cover. Admin only, same as every other write here. */
    @PostMapping("/{id}/cover")
    public ResponseEntity<Map<String, String>> uploadCover(@PathVariable Integer id,
                                                            @RequestParam("file") MultipartFile file)
            throws IOException {
        productCoverService.saveCover(file, id);
        return ResponseEntity.ok(Map.of("message", "Cover uploaded successfully."));
    }

    /** Serves the stored cover image. Public, same as every other read here - the storefront needs it. */
    @GetMapping("/{id}/cover")
    public ResponseEntity<byte[]> getCover(@PathVariable Integer id) {
        ProductCover cover = productCoverService.getCover(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(cover.getContentType()))
                .body(cover.getImageData());
    }
}

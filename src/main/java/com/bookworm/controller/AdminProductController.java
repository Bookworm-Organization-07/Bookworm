package com.bookworm.controller;

import com.bookworm.dto.admin.AdminProductResponse;
import com.bookworm.dto.admin.UpdateProductFlagsRequest;
import com.bookworm.dto.admin.UploadBatchResponse;
import com.bookworm.entity.Admin;
import com.bookworm.entity.UploadBatch;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.AdminRepository;
import com.bookworm.repository.UploadBatchRepository;
import com.bookworm.service.AdminProductService;
import com.bookworm.service.ExcelProductImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ExcelProductImportService excelProductImportService;
    private final AdminProductService adminProductService;
    private final AdminRepository adminRepository;
    private final UploadBatchRepository uploadBatchRepository;

    public AdminProductController(
            ExcelProductImportService excelProductImportService,
            AdminProductService adminProductService,
            AdminRepository adminRepository,
            UploadBatchRepository uploadBatchRepository) {
        this.excelProductImportService = excelProductImportService;
        this.adminProductService = adminProductService;
        this.adminRepository = adminRepository;
        this.uploadBatchRepository = uploadBatchRepository;
    }

    @GetMapping
    public List<AdminProductResponse> listAll() {
        return adminProductService.listAll();
    }

    @PatchMapping("/{productId}/flags")
    public AdminProductResponse updateFlags(@PathVariable Integer productId, @RequestBody UpdateProductFlagsRequest request) {
        return adminProductService.updateFlags(productId, request);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<UploadBatchResponse> upload(@RequestParam("file") MultipartFile file, Authentication authentication) {
        Admin admin = adminRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found: " + authentication.getName()));

        UploadBatch batch = excelProductImportService.importFromExcel(file, admin);
        return ResponseEntity.ok(UploadBatchResponse.from(batch));
    }

    @GetMapping("/upload-batches")
    public List<UploadBatchResponse> listUploadBatches() {
        return uploadBatchRepository.findAllByOrderByUploadedAtDesc().stream()
                .map(UploadBatchResponse::from)
                .toList();
    }
}

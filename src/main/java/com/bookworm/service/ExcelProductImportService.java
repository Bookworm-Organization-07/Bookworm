package com.bookworm.service;

import com.bookworm.entity.Admin;
import com.bookworm.entity.UploadBatch;
import com.bookworm.entity.enums.UploadStatus;
import com.bookworm.repository.UploadBatchRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Bulk product/library-package importer (TODO #10) driven by Apache POI.
 * Orchestrates parsing; each row is committed independently via {@link ProductRowImportService}
 * so one malformed row never rolls back the rest of the batch.
 */
@Service
public class ExcelProductImportService {

    private final ProductRowImportService productRowImportService;
    private final UploadBatchRepository uploadBatchRepository;

    public ExcelProductImportService(ProductRowImportService productRowImportService, UploadBatchRepository uploadBatchRepository) {
        this.productRowImportService = productRowImportService;
        this.uploadBatchRepository = uploadBatchRepository;
    }

    public record ParsedProductRow(
            int rowNumber,
            String prodName,
            String attributeSet,
            String typeLanguageCategory,
            String availability,
            String author,
            String description,
            BigDecimal price,
            BigDecimal specialPrice,
            String publisher,
            String nameEnglish,
            String metaKeyword,
            Integer status) {
    }

    public UploadBatch importFromExcel(MultipartFile file, Admin admin) {
        UploadBatch batch = UploadBatch.builder()
                .admin(admin)
                .fileName(file.getOriginalFilename())
                .totalRows(0)
                .successRows(0)
                .failedRows(0)
                .status(UploadStatus.PROCESSING)
                .build();
        batch = uploadBatchRepository.save(batch);

        StringBuilder log = new StringBuilder();
        int total = 0;
        int success = 0;
        int failed = 0;

        try (InputStream in = file.getInputStream(); Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columnIndex = buildColumnIndex(headerRow, formatter);

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String prodName = readString(row, columnIndex.get("prod name"), formatter);
                if (prodName == null || prodName.isBlank()) continue; // blank trailing rows don't count

                total++;
                ParsedProductRow parsed = new ParsedProductRow(
                        r + 1,
                        prodName,
                        readString(row, columnIndex.get("_attribute_set"), formatter),
                        readString(row, columnIndex.get("type/language/category"), formatter),
                        readString(row, columnIndex.get("availability"), formatter),
                        readString(row, columnIndex.get("author"), formatter),
                        readString(row, columnIndex.get("description"), formatter),
                        readBigDecimal(row, columnIndex.get("price"), formatter),
                        readBigDecimal(row, columnIndex.get("special_price"), formatter),
                        readString(row, columnIndex.get("publisher"), formatter),
                        readString(row, columnIndex.get("product_name_in_english"), formatter),
                        readString(row, columnIndex.get("meta_keyword"), formatter),
                        readInteger(row, columnIndex.get("status"), formatter));

                try {
                    ProductRowImportService.RowResult result = productRowImportService.importRow(parsed);
                    success++;
                    log.append("Row ").append(parsed.rowNumber()).append(": ").append(result.message()).append('\n');
                } catch (Exception ex) {
                    failed++;
                    log.append("Row ").append(parsed.rowNumber()).append(": FAILED - ").append(ex.getMessage()).append('\n');
                }
            }

            batch.setTotalRows(total);
            batch.setSuccessRows(success);
            batch.setFailedRows(failed);
            batch.setErrorLog(log.toString());
            batch.setStatus(UploadStatus.COMPLETED);
        } catch (IOException | RuntimeException ex) {
            batch.setStatus(UploadStatus.FAILED);
            batch.setErrorLog("Could not read workbook: " + ex.getMessage());
        }

        return uploadBatchRepository.save(batch);
    }

    private Map<String, Integer> buildColumnIndex(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> index = new HashMap<>();
        for (Cell cell : headerRow) {
            String header = formatter.formatCellValue(cell).trim().toLowerCase();
            if (!header.isEmpty()) {
                index.put(header, cell.getColumnIndex());
            }
        }
        return index;
    }

    private String readString(Row row, Integer colIndex, DataFormatter formatter) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private BigDecimal readBigDecimal(Row row, Integer colIndex, DataFormatter formatter) {
        String raw = readString(row, colIndex, formatter);
        if (raw == null) return null;
        try {
            return new BigDecimal(raw.replaceAll("[^0-9.\\-]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer readInteger(Row row, Integer colIndex, DataFormatter formatter) {
        BigDecimal value = readBigDecimal(row, colIndex, formatter);
        return value != null ? value.intValue() : null;
    }
}

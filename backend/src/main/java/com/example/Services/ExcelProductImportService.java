package com.example.Services;

import com.example.dto.BulkUploadResultDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ExcelProductImportService {

    private final ProductRowImportService productRowImportService;
    private final ProductCoverService productCoverService;

    public ExcelProductImportService(ProductRowImportService productRowImportService,
                                     ProductCoverService productCoverService) {
        this.productRowImportService = productRowImportService;
        this.productCoverService = productCoverService;
    }

    public record ParsedProductRow(
            int rowNumber,
            String prodName,
            String nameEnglish,
            String attributeSet,
            String typeLanguageCategory,
            String availability,
            String author,
            String publisher,
            String description,
            String shortDescription,
            String isPackage,
            BigDecimal price,
            BigDecimal specialPrice,
            String coverId) {
    }

   
    public BulkUploadResultDto importFromExcel(MultipartFile file, List<MultipartFile> coverFiles) {
        BulkUploadResultDto result = new BulkUploadResultDto();

        if (file == null || file.isEmpty()) {
            result.setLog("No file was uploaded.");
            return result;
        }

        
        List<MultipartFile> unmatchedCovers = new ArrayList<>();
        if (coverFiles != null) {
            for (MultipartFile candidate : coverFiles) {
                String contentType = candidate.getContentType();
                if (contentType != null && contentType.startsWith("image/")) {
                    unmatchedCovers.add(candidate);
                }
            }
        }

        StringBuilder log = new StringBuilder();
        int total = 0;
        int created = 0;
        int skipped = 0;
        int failed = 0;

        try (InputStream in = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(in)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columnIndex = buildColumnIndex(headerRow, formatter);

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                String prodName = readString(row, columnIndex.get("prod name"), formatter);
                if (prodName == null) {
                    continue; // a fully blank trailing row - not a real data row
                }

                total++;
                ParsedProductRow parsed = new ParsedProductRow(
                        r + 1, // spreadsheet row numbers start at 1, and row 1 is the header
                        prodName,
                        readString(row, columnIndex.get("product_name_in_english"), formatter),
                        readString(row, columnIndex.get("_attribute_set"), formatter),
                        readString(row, columnIndex.get("type/language/category"), formatter),
                        readString(row, columnIndex.get("availability"), formatter),
                        readString(row, columnIndex.get("author"), formatter),
                        readString(row, columnIndex.get("publisher"), formatter),
                        readString(row, columnIndex.get("description"), formatter),
                        readString(row, columnIndex.get("short_description"), formatter),
                        readString(row, columnIndex.get("is_package"), formatter),
                        readBigDecimal(row, columnIndex.get("price"), formatter),
                        readBigDecimal(row, columnIndex.get("special_price"), formatter),
                        readString(row, columnIndex.get("cover_id"), formatter));

                try {
                    ProductRowImportService.RowResult rowResult = productRowImportService.importRow(parsed);
                    if (rowResult.outcome() == ProductRowImportService.Outcome.CREATED) {
                        created++;
                    } else {
                        skipped++;
                    }
                    log.append("Row ").append(parsed.rowNumber()).append(": ")
                            .append(rowResult.message());

                    if (rowResult.productId() != null) {
                        String coverNote = attachCover(parsed, rowResult.productId(), unmatchedCovers);
                        if (!coverNote.isEmpty()) {
                            log.append(' ').append(coverNote);
                        }
                    }
                    log.append('\n');
                } catch (Exception ex) {
                    failed++;
                    log.append("Row ").append(parsed.rowNumber()).append(": FAILED - ")
                            .append(ex.getMessage()).append('\n');
                }
            }

        } catch (IOException | RuntimeException ex) {
            log.append("Could not read the file: ").append(ex.getMessage()).append('\n');
        }

        result.setTotalRows(total);
        result.setCreatedRows(created);
        result.setSkippedRows(skipped);
        result.setFailedRows(failed);
        result.setLog(log.toString());
        return result;
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

   
    private String attachCover(ParsedProductRow row, int productId, List<MultipartFile> unmatchedCovers) {
        MultipartFile match = findCoverById(row.coverId(), unmatchedCovers);
        if (match == null) {
            return "";
        }
        unmatchedCovers.remove(match);
        String displayName = basename(match.getOriginalFilename());

        try {
            productCoverService.saveCover(match, productId);
            return "Cover: matched '" + displayName + "'.";
        } catch (IOException | RuntimeException ex) {
            return "Cover: '" + displayName + "' could not be saved - " + ex.getMessage();
        }
    }

    private MultipartFile findCoverById(String coverId, List<MultipartFile> unmatchedCovers) {
        if (coverId == null || coverId.isBlank()) {
            return null;
        }
        for (MultipartFile cover : unmatchedCovers) {
            String stem = stripExtension(basename(cover.getOriginalFilename())).trim();
            if (stem.equalsIgnoreCase(coverId.trim())) {
                return cover;
            }
        }
        return null;
    }

   
    private String basename(String filename) {
        if (filename == null) {
            return "";
        }
        int cut = Math.max(filename.lastIndexOf('/'), filename.lastIndexOf('\\'));
        return cut >= 0 ? filename.substring(cut + 1) : filename;
    }

    private String stripExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    private String readString(Row row, Integer colIndex, DataFormatter formatter) {
        if (colIndex == null) {
            return null; // that column doesn't exist in this file at all
        }
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        String value = formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private BigDecimal readBigDecimal(Row row, Integer colIndex, DataFormatter formatter) {
        String raw = readString(row, colIndex, formatter);
        if (raw == null) {
            return null;
        }
        try {
            return new BigDecimal(raw.replaceAll("[^0-9.\\-]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

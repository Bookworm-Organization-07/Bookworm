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

/**
 * Reads an admin-uploaded .xlsx catalogue file (the "Prod Master Table"
 * layout) and turns each row into a product.
 *
 * This class only knows how to READ THE SPREADSHEET - it finds the right
 * column by its header NAME (not its position, so column order never
 * matters), pulls out one row's worth of plain values, and hands them to
 * ProductRowImportService, which is the class that actually knows what a
 * Product looks like. Splitting it this way means "how do I read an
 * Excel cell" and "how do I turn a book into a Product row" never get
 * tangled together in the same method.
 *
 * Every row is imported inside its own transaction (see
 * ProductRowImportService), so one bad row - a missing price, a typo in
 * a column - never rolls back every row already imported before it.
 */
@Service
public class ExcelProductImportService {

    /** Below this many letters/digits, a title (or filename) is too short to match safely - see findCoverByTitle. */
    private static final int MIN_TITLE_LENGTH_FOR_MATCH = 3;

    private final ProductRowImportService productRowImportService;
    private final ProductCoverService productCoverService;

    public ExcelProductImportService(ProductRowImportService productRowImportService,
                                     ProductCoverService productCoverService) {
        this.productRowImportService = productRowImportService;
        this.productCoverService = productCoverService;
    }

    /** One row's worth of plain values, read straight off the spreadsheet - nothing looked up yet. */
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

    /**
     * coverFiles is whatever the admin selected alongside the
     * spreadsheet - a whole folder of jpgs, in no particular order.
     * Each row is matched to a cover either by the spreadsheet's
     * "cover_id" column (see findCoverById) or, when that column isn't
     * present, by comparing the cover's filename to the row's title
     * (see findCoverByTitle). A row with no cover_id and no matching
     * filename simply gets no cover - that is reported in the log the
     * same as everything else here.
     */
    public BulkUploadResultDto importFromExcel(MultipartFile file, List<MultipartFile> coverFiles) {
        BulkUploadResultDto result = new BulkUploadResultDto();

        if (file == null || file.isEmpty()) {
            result.setLog("No file was uploaded.");
            return result;
        }

        // Selecting a whole folder picks up EVERYTHING in it, not just
        // images - this catalogue's own cover folder also has the PDF
        // ebook behind some of these titles sitting right next to its
        // jpg. Filtering to actual images here, once, means a title
        // whose filename happens to also resemble a same-named PDF can
        // never end up with that PDF as its "cover" - which would just
        // show as a broken image on the site.
        //
        // Files also get removed from this list as they are matched, so
        // the same cover image can never be attached to two different
        // books.
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

                    // productId is only ever absent for a package row
                    // (see ProductRowImportService) - a title that was
                    // CREATED here, or one that already existed and was
                    // SKIPPED, both have a real product a cover can
                    // attach to.
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

    /**
     * Reads the header row once and remembers which column number each
     * header text is at (lower-cased and trimmed, so "Prod Name" and
     * "prod name " both match "prod name"). This is what lets the
     * spreadsheet's columns be in any order.
     */
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

    /**
     * Looks for a still-unmatched cover whose filename (minus extension)
     * exactly matches this row's cover_id. A match found here is
     * removed from the pool immediately, so the same cover image can
     * never be attached to two different books further down the
     * spreadsheet. Never throws - a cover that fails to save is
     * reported in the log same as everything else here, but it does
     * not undo the product row that was already created or found for it.
     *
     * The real "Prod Master Table" export has no cover_id column at all
     * (see SETUP.md's Bulk Upload description: covers are "matched to
     * each book by comparing image filenames to titles"), so
     * findCoverById never matches anything against that file.
     * findCoverByTitle is the fallback that actually implements that
     * documented filename-to-title matching.
     */
    private String attachCover(ParsedProductRow row, int productId, List<MultipartFile> unmatchedCovers) {
        MultipartFile match = findCoverById(row.coverId(), unmatchedCovers);
        if (match == null) {
            match = findCoverByTitle(row, unmatchedCovers);
        }
        if (match == null) {
            return "";
        }
        unmatchedCovers.remove(match);

        try {
            productCoverService.saveCover(match, productId);
            return "Cover: matched '" + match.getOriginalFilename() + "'.";
        } catch (IOException | RuntimeException ex) {
            return "Cover: '" + match.getOriginalFilename() + "' could not be saved - " + ex.getMessage();
        }
    }

    /** cover_id "105" matches a cover file named "105.jpg", "105.png", etc. - exact, case-insensitive. */
    private MultipartFile findCoverById(String coverId, List<MultipartFile> unmatchedCovers) {
        if (coverId == null || coverId.isBlank()) {
            return null;
        }
        for (MultipartFile cover : unmatchedCovers) {
            String stem = stripExtension(cover.getOriginalFilename()).trim();
            if (stem.equalsIgnoreCase(coverId.trim())) {
                return cover;
            }
        }
        return null;
    }

    /**
     * Filenames in the covers folder are close to, but not exactly,
     * product_name_in_english - extra words ("Cover", "-v2", ".web"),
     * digits, or different spacing/hyphenation are common (e.g. "Bolaki
     * Hade" / "BolakiHadeCover.jpg"). Comparing both with everything but
     * letters and digits stripped out, and accepting a match either
     * direction contains the other, handles that without needing an
     * exact title. MIN_TITLE_LENGTH_FOR_MATCH keeps a very short title
     * (or a stripped-down empty one) from matching almost any filename.
     */
    private MultipartFile findCoverByTitle(ParsedProductRow row, List<MultipartFile> unmatchedCovers) {
        String title = normalizeForMatch(row.nameEnglish());
        if (title.isEmpty()) {
            title = normalizeForMatch(row.prodName());
        }
        if (title.length() < MIN_TITLE_LENGTH_FOR_MATCH) {
            return null;
        }
        for (MultipartFile cover : unmatchedCovers) {
            String stem = normalizeForMatch(stripExtension(cover.getOriginalFilename()));
            if (stem.length() >= MIN_TITLE_LENGTH_FOR_MATCH && (stem.contains(title) || title.contains(stem))) {
                return cover;
            }
        }
        return null;
    }

    private String normalizeForMatch(String value) {
        return value == null ? "" : value.toLowerCase().replaceAll("[^a-z0-9]", "");
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
            // Strips out anything that isn't a digit, a dot or a minus
            // sign, in case the cell has stray currency symbols or spaces.
            return new BigDecimal(raw.replaceAll("[^0-9.\\-]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

package com.example.dto;

/**
 * What the admin sees after uploading a catalogue spreadsheet: how many
 * data rows were found, how many became new products, how many were
 * skipped (already existed, or were a library-package row), how many
 * failed outright, and a line-by-line log explaining every single one.
 */
public class BulkUploadResultDto {

    private int totalRows;
    private int createdRows;
    private int skippedRows;
    private int failedRows;
    private String log;

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getCreatedRows() {
        return createdRows;
    }

    public void setCreatedRows(int createdRows) {
        this.createdRows = createdRows;
    }

    public int getSkippedRows() {
        return skippedRows;
    }

    public void setSkippedRows(int skippedRows) {
        this.skippedRows = skippedRows;
    }

    public int getFailedRows() {
        return failedRows;
    }

    public void setFailedRows(int failedRows) {
        this.failedRows = failedRows;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}

package com.bookworm.dto.admin;

import com.bookworm.entity.UploadBatch;

import java.time.LocalDateTime;

public record UploadBatchResponse(
        Integer uploadBatchId,
        String fileName,
        Integer totalRows,
        Integer successRows,
        Integer failedRows,
        String status,
        String errorLog,
        LocalDateTime uploadedAt) {

    public static UploadBatchResponse from(UploadBatch batch) {
        return new UploadBatchResponse(
                batch.getUploadBatchId(),
                batch.getFileName(),
                batch.getTotalRows(),
                batch.getSuccessRows(),
                batch.getFailedRows(),
                batch.getStatus().name(),
                batch.getErrorLog(),
                batch.getUploadedAt());
    }
}

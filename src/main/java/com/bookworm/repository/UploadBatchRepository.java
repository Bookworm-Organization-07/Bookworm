package com.bookworm.repository;

import com.bookworm.entity.UploadBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadBatchRepository extends JpaRepository<UploadBatch, Integer> {

    List<UploadBatch> findAllByOrderByUploadedAtDesc();
}

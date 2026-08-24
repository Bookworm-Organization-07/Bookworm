package com.example.Services;

import com.example.Repository.ProductRepository;
import com.example.Repository.ReadBookRepository;
import com.example.models.Product;
import com.example.models.ReadBook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ReadBookService {

    private final ReadBookRepository readBookRepository;
    private final ProductRepository productRepository;

    public ReadBookService(ReadBookRepository readBookRepository,
                           ProductRepository productRepository) {
        this.readBookRepository = readBookRepository;
        this.productRepository = productRepository;
    }
    
    public void savePdf(MultipartFile file, int productId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        ReadBook readBook = readBookRepository.findByProduct_ProductId(productId)
                .orElseGet(ReadBook::new);

        readBook.setProduct(product);
        readBook.setFileName(file.getOriginalFilename());
        readBook.setPdfData(file.getBytes());

        readBookRepository.save(readBook);
    }

    public ReadBook getPdfByProductId(int productId) {
        return readBookRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("No PDF uploaded for this title."));
    }

    public byte[] readBook(int productId) {
        ReadBook readBook = getPdfByProductId(productId);

        if (readBook.getPdfData() == null || readBook.getPdfData().length == 0) {
            throw new IllegalArgumentException("No PDF uploaded for this title.");
        }
        return readBook.getPdfData();
    }
}

package com.example.Services;

import com.example.Repository.ProductCoverRepository;
import com.example.Repository.ProductRepository;
import com.example.models.Product;
import com.example.models.ProductCover;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ProductCoverService {

    private final ProductCoverRepository productCoverRepository;
    private final ProductRepository productRepository;

    public ProductCoverService(ProductCoverRepository productCoverRepository,
                               ProductRepository productRepository) {
        this.productCoverRepository = productCoverRepository;
        this.productRepository = productRepository;
    }
    public void saveCover(MultipartFile file, int productId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was uploaded.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        ProductCover cover = productCoverRepository.findByProduct_ProductId(productId)
                .orElseGet(ProductCover::new);

        cover.setProduct(product);
        cover.setContentType(file.getContentType());
        cover.setImageData(file.getBytes());
        productCoverRepository.save(cover);

        product.setProductImage("/api/products/" + productId + "/cover");
        productRepository.save(product);
    }

    public ProductCover getCover(int productId) {
        return productCoverRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("No cover uploaded for this title."));
    }
}

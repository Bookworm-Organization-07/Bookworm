package com.example.Services;

import com.example.Repository.MyLibraryRepository;
import com.example.dto.MyLibraryItemResponseDto;
import com.example.dto.ProductResponseDto;
import com.example.models.LibraryAccessType;
import com.example.models.MyLibrary;
import com.example.models.Product;
import com.example.models.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MyLibraryServiceImpl implements MyLibraryService {

    private final MyLibraryRepository myLibraryRepository;
    private final ReadBookService readBookService;

    public MyLibraryServiceImpl(MyLibraryRepository myLibraryRepository,
                                ReadBookService readBookService) {
        this.myLibraryRepository = myLibraryRepository;
        this.readBookService = readBookService;
    }

    /** Everything still inside its access window - rented AND lent books together. */
    @Override
    @Transactional(readOnly = true)
    public List<MyLibraryItemResponseDto> getUserLibrary(Integer userId) {
        return myLibraryRepository
                .findByUser_UserIdAndEndDateAfter(userId, LocalDate.now())
                .stream()
                .filter(ml -> ml.getProduct() != null)
                .map(this::toDto)
                .toList();
    }

    /**
     * The borrowing check happens here, not in the controller: a reader
     * may only open a PDF they currently have out - whether they rented
     * it or borrowed it through a library package makes no difference
     * for reading.
     */
    @Override
    @Transactional(readOnly = true)
    public byte[] readLibraryBook(Integer userId, Integer productId) {
        boolean allowed = myLibraryRepository
                .existsByUser_UserIdAndProduct_ProductIdAndEndDateAfter(
                        userId, productId, LocalDate.now());

        if (!allowed) {
            throw new IllegalArgumentException("This title is not in your library.");
        }
        return readBookService.readBook(productId);
    }

    /**
     * Called once a RENT checkout succeeds (see CheckoutService).
     *
     * If the reader already has this exact book rented and it has not
     * run out yet, renting it again just pushes the end date further
     * out - it should never create a second row for the same book.
     */
    @Override
    @Transactional
    public void recordRental(User user, Product product, int rentDays) {
        LocalDate today = LocalDate.now();
        LocalDate newEndDate = today.plusDays(rentDays);

        Optional<MyLibrary> existingRental = myLibraryRepository
                .findByUser_UserIdAndProduct_ProductIdAndAccessTypeAndEndDateAfter(
                        user.getUserId(), product.getProductId(), LibraryAccessType.RENT, today);

        if (existingRental.isPresent()) {
            MyLibrary rental = existingRental.get();
            if (newEndDate.isAfter(rental.getEndDate())) {
                rental.setEndDate(newEndDate);
                myLibraryRepository.save(rental);
            }
            return;
        }

        MyLibrary rental = new MyLibrary();
        rental.setUser(user);
        rental.setProduct(product);
        rental.setAccessType(LibraryAccessType.RENT);
        rental.setStartDate(today);
        rental.setEndDate(newEndDate);
        // No library package, and books_allowed / books_taken only mean
        // something for a LEND row - leave them all null on purpose.
        myLibraryRepository.save(rental);
    }

    private MyLibraryItemResponseDto toDto(MyLibrary ml) {
        Product product = ml.getProduct();

        ProductResponseDto productDto = new ProductResponseDto();
        productDto.setProductId(product.getProductId());
        productDto.setProductName(product.getProductName());
        productDto.setProductImage(product.getProductImage());
        if (product.getAuthor() != null) {
            productDto.setAuthorName(product.getAuthor().getName());
        }

        MyLibraryItemResponseDto dto = new MyLibraryItemResponseDto();
        dto.setMyLibId(ml.getMyLibId());
        dto.setAccessType(ml.getAccessType().name());

        // A RENT row has no library package, so only fill these in when
        // one is actually there - otherwise this would throw a
        // NullPointerException for every rented book.
        if (ml.getLibraryPackage() != null) {
            dto.setPackageId(ml.getLibraryPackage().getPackageId());
            dto.setPackageName(ml.getLibraryPackage().getName());
        }

        dto.setStartDate(ml.getStartDate());
        dto.setEndDate(ml.getEndDate());
        dto.setProduct(productDto);
        return dto;
    }
}

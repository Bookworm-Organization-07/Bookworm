package com.bookworm.service;

import com.bookworm.dto.library.LibraryEntryResponse;
import com.bookworm.dto.library.LibraryPackageResponse;
import com.bookworm.dto.library.MembershipResponse;
import com.bookworm.dto.library.MyLibraryResponse;
import com.bookworm.entity.Library;
import com.bookworm.entity.Product;
import com.bookworm.entity.User;
import com.bookworm.entity.UserLibraryMembership;
import com.bookworm.entity.enums.LibraryAccessType;
import com.bookworm.entity.enums.LibraryStatus;
import com.bookworm.entity.enums.MembershipStatus;
import com.bookworm.exception.ResourceNotFoundException;
import com.bookworm.repository.LibraryPackageRepository;
import com.bookworm.repository.LibraryRepository;
import com.bookworm.repository.ProductRepository;
import com.bookworm.repository.UserLibraryMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** BRD §6 Lending Library — lending against a purchased library package. */
@Service
@Transactional
public class LibraryService {

    private final LibraryPackageRepository libraryPackageRepository;
    private final UserLibraryMembershipRepository membershipRepository;
    private final LibraryRepository libraryRepository;
    private final ProductRepository productRepository;
    private final RoyaltyService royaltyService;

    public LibraryService(
            LibraryPackageRepository libraryPackageRepository,
            UserLibraryMembershipRepository membershipRepository,
            LibraryRepository libraryRepository,
            ProductRepository productRepository,
            RoyaltyService royaltyService) {
        this.libraryPackageRepository = libraryPackageRepository;
        this.membershipRepository = membershipRepository;
        this.libraryRepository = libraryRepository;
        this.productRepository = productRepository;
        this.royaltyService = royaltyService;
    }

    @Transactional(readOnly = true)
    public List<LibraryPackageResponse> listPackages() {
        return libraryPackageRepository.findByIsActiveTrue().stream().map(LibraryPackageResponse::from).toList();
    }

    public LibraryEntryResponse lend(User user, Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if (!Boolean.TRUE.equals(product.getIsActive()) || !Boolean.TRUE.equals(product.getIsLendable())) {
            throw new ResourceNotFoundException("Product not eligible for lending: " + productId);
        }

        boolean alreadyLent = libraryRepository
                .findByUser_UserIdAndProduct_ProductIdAndAccessTypeAndStatus(
                        user.getUserId(), productId, LibraryAccessType.LENT, LibraryStatus.ACTIVE)
                .isPresent();
        if (alreadyLent) {
            throw new IllegalStateException("You already have this title lent out.");
        }

        UserLibraryMembership membership = membershipRepository
                .findByUser_UserIdAndStatusOrderByExpiryDateAsc(user.getUserId(), MembershipStatus.ACTIVE)
                .stream()
                .filter(m -> !m.getExpiryDate().isBefore(LocalDate.now()))
                .filter(m -> m.getBooksUsed() < m.getLibraryPackage().getBooksAllowed())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "You need an active library package with lending capacity remaining. Purchase one from the Lending Library page."));

        Library entry = Library.builder()
                .user(user)
                .product(product)
                .accessType(LibraryAccessType.LENT)
                .membership(membership)
                .startDate(LocalDateTime.now())
                .expiryDate(membership.getExpiryDate().atStartOfDay())
                .status(LibraryStatus.ACTIVE)
                .build();
        entry = libraryRepository.save(entry);

        membership.setBooksUsed(membership.getBooksUsed() + 1);
        if (membership.getBooksUsed() >= membership.getLibraryPackage().getBooksAllowed()) {
            membership.setStatus(MembershipStatus.VOID);
        }
        membershipRepository.save(membership);

        royaltyService.calculateForLend(entry);

        return LibraryEntryResponse.from(entry);
    }

    public MyLibraryResponse getMyLibrary(User user) {
        LocalDate today = LocalDate.now();

        List<UserLibraryMembership> memberships = membershipRepository.findByUser_UserId(user.getUserId());
        for (UserLibraryMembership m : memberships) {
            if (m.getStatus() == MembershipStatus.ACTIVE && m.getExpiryDate().isBefore(today)) {
                m.setStatus(MembershipStatus.EXPIRED);
            }
        }

        List<Library> entries = libraryRepository.findByUser_UserIdOrderByStartDateDesc(user.getUserId());
        LocalDateTime now = LocalDateTime.now();
        for (Library entry : entries) {
            if (entry.getStatus() == LibraryStatus.ACTIVE && entry.getExpiryDate().isBefore(now)) {
                entry.setStatus(LibraryStatus.EXPIRED);
            }
        }

        List<Library> active = entries.stream().filter(e -> e.getStatus() == LibraryStatus.ACTIVE).toList();

        return new MyLibraryResponse(
                memberships.stream().map(MembershipResponse::from).toList(),
                active.stream().map(LibraryEntryResponse::from).toList());
    }
}

package com.example.dto;

import java.time.LocalDate;

public class MyLibraryItemResponseDto {

    private Integer myLibId;

    /** "RENT" or "LEND" - tells the frontend which label to show. */
    private String accessType;

    /** Null when accessType is RENT - a plain rental has no package. */
    private Integer packageId;

    /** Null when accessType is RENT - a plain rental has no package. */
    private String packageName;

    private LocalDate startDate;
    private LocalDate endDate;
    private ProductResponseDto product;

    public Integer getMyLibId() {
        return myLibId;
    }

    public void setMyLibId(Integer myLibId) {
        this.myLibId = myLibId;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public Integer getPackageId() {
        return packageId;
    }

    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ProductResponseDto getProduct() {
        return product;
    }

    public void setProduct(ProductResponseDto product) {
        this.product = product;
    }
}

package com.example.dto;

import java.math.BigDecimal;

/**
 * What the "add a single book" form on the admin Bulk Upload page sends.
 * The fields mirror one row of the bulk-upload spreadsheet on purpose -
 * see ProductController.quickAdd, which hands this straight to the same
 * ProductRowImportService.importRow that the spreadsheet path uses, so
 * a single book behaves exactly like one bulk-uploaded row (same
 * placeholder ISBN, same auto-calculated rent price, and so on).
 */
public class ManualProductRequest {

    private String prodName;
    private String nameEnglish;
    private String type;
    private String language;
    private String genre;
    private String author;
    private String publisher;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private BigDecimal specialPrice;
    private boolean rentable;
    private boolean library;

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getSpecialPrice() {
        return specialPrice;
    }

    public void setSpecialPrice(BigDecimal specialPrice) {
        this.specialPrice = specialPrice;
    }

    public boolean isRentable() {
        return rentable;
    }

    public void setRentable(boolean rentable) {
        this.rentable = rentable;
    }

    public boolean isLibrary() {
        return library;
    }

    public void setLibrary(boolean library) {
        this.library = library;
    }
}

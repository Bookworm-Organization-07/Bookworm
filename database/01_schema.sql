-- =====================================================================
-- Bookworm Delta - Schema
-- =====================================================================
-- Target : MySQL 8.x
-- Source : Bookworm (BOOKWORM_PRO) JPA entities, cross-checked against
--          Reverse_Engineered_Database_Design.xlsx
--
-- Tables are declared in dependency order. That same order is the order
-- the entity classes are listed in BUILD_ORDER.md, so a table never
-- references something that does not exist yet.
--
-- Column names match the @Column / @JoinColumn names on the entities
-- exactly, because application.properties runs with
-- spring.jpa.hibernate.ddl-auto=validate: if this file and the entities
-- ever drift apart, the application refuses to start.
-- =====================================================================

DROP DATABASE IF EXISTS bookworm_delta;
CREATE DATABASE bookworm_delta
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE bookworm_delta;

CREATE TABLE `user` (
    User_ID       INT AUTO_INCREMENT PRIMARY KEY,
    User_Name     VARCHAR(80)  NOT NULL,
    User_Email    VARCHAR(80)  NOT NULL UNIQUE,
    User_Phone    VARCHAR(20),
    User_Address  TEXT,
    User_Password VARCHAR(255) NOT NULL,
    is_Admin      BOOLEAN      NOT NULL DEFAULT FALSE,
    Join_Date     DATE
);

CREATE TABLE author (
    Author_ID INT AUTO_INCREMENT PRIMARY KEY,
    Name      VARCHAR(100) NOT NULL,
    Bio       TEXT
);

CREATE TABLE publisher (
    Publisher_ID INT AUTO_INCREMENT PRIMARY KEY,
    Name         VARCHAR(100) NOT NULL,
    Email        VARCHAR(80)  NOT NULL UNIQUE
);

CREATE TABLE `language` (
    Language_id   INT AUTO_INCREMENT PRIMARY KEY,
    Language_Desc VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE genere (
    Genere_id   INT AUTO_INCREMENT PRIMARY KEY,
    Genere_Desc VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE product_type_master (
    Type_Id   INT AUTO_INCREMENT PRIMARY KEY,
    Type_Desc VARCHAR(50) NOT NULL UNIQUE
);

-- ---------------------------------------------------------------------
-- 7. attribute
-- ---------------------------------------------------------------------
-- Column renamed from "Attribute Desc" (a name containing a space) to
-- Attribute_Desc. See BACKEND_FIXES.md #F-01.
CREATE TABLE attribute (
    Attribute_Id   INT AUTO_INCREMENT PRIMARY KEY,
    Attribute_Desc VARCHAR(255) NOT NULL
);

-- ---------------------------------------------------------------------
-- 8. product
-- ---------------------------------------------------------------------
-- Attribute_Id is deliberately absent: a product's attributes live in
-- product_attribute. This is improvement #1 from the spreadsheet's
-- Overview sheet. See BACKEND_FIXES.md #F-02.
CREATE TABLE product (
    product_id                   INT AUTO_INCREMENT PRIMARY KEY,
    product_name                 VARCHAR(150)   NOT NULL,
    -- The book's title in its own script (Marathi/Hindi/Konkani/etc).
    -- product_name_english is its English name/transliteration, so a
    -- reader who cannot type Devanagari can still find the book by
    -- searching for the English name - see ProductRepository's search
    -- query. Nullable: plenty of titles (English ones, or ones entered
    -- by hand) have no separate English name at all.
    product_name_english         VARCHAR(150),
    product_type                 INT,
    product_author               INT,
    product_publisher            INT,
    product_lang                 INT,
    product_genere               INT,
    product_baseprice            DECIMAL(10, 2) NOT NULL,
    product_offerprice           DECIMAL(10, 2),
    discount_percent             DECIMAL(5, 2),
    royalty_percent              DECIMAL(5, 2),
    product_off_price_expirydate DATE,
    product_description_short    VARCHAR(255),
    product_description_long     TEXT,
    product_isbn                 VARCHAR(20)    NOT NULL UNIQUE,
    is_rentable                  BOOLEAN        NOT NULL DEFAULT FALSE,
    is_library                   BOOLEAN        NOT NULL DEFAULT FALSE,
    rent_per_day                 DECIMAL(5, 2),
    min_rent_days                INT            NOT NULL DEFAULT 0,
    product_Image                VARCHAR(255),

    CONSTRAINT fk_product_type      FOREIGN KEY (product_type)      REFERENCES product_type_master (Type_Id),
    CONSTRAINT fk_product_author    FOREIGN KEY (product_author)    REFERENCES author (Author_ID),
    CONSTRAINT fk_product_publisher FOREIGN KEY (product_publisher) REFERENCES publisher (Publisher_ID),
    CONSTRAINT fk_product_lang      FOREIGN KEY (product_lang)      REFERENCES `language` (Language_id),
    CONSTRAINT fk_product_genere    FOREIGN KEY (product_genere)    REFERENCES genere (Genere_id)
);

-- ---------------------------------------------------------------------
-- 9. product_attribute
-- ---------------------------------------------------------------------
-- Column renamed from atttribute_value (three t's) to attribute_value.
-- UNIQUE(product_id, attribute_id) is improvement #3 from the
-- spreadsheet's Overview sheet. See BACKEND_FIXES.md #F-01.
CREATE TABLE product_attribute (
    prod_att_id     INT AUTO_INCREMENT PRIMARY KEY,
    product_id      INT NOT NULL,
    attribute_id    INT NOT NULL,
    attribute_value VARCHAR(255),

    CONSTRAINT fk_prodattr_product   FOREIGN KEY (product_id)   REFERENCES product (product_id),
    CONSTRAINT fk_prodattr_attribute FOREIGN KEY (attribute_id) REFERENCES attribute (Attribute_Id),
    CONSTRAINT uq_prodattr           UNIQUE (product_id, attribute_id)
);

-- ---------------------------------------------------------------------
-- 10. beneficiary
-- ---------------------------------------------------------------------
-- A beneficiary is a reusable identity (a person, estate, or publisher
-- who receives royalties) - it is NOT tied to one product. Which
-- products it currently receives royalty for lives in
-- beneficiary_assignment (table 23) instead, so the same beneficiary
-- can be picked for any number of books instead of being re-entered
-- (bank details and all) for each one.
CREATE TABLE beneficiary (
    beneficiary_id          INT AUTO_INCREMENT PRIMARY KEY,
    beneficiary_name        VARCHAR(100) NOT NULL,
    -- 'Author', 'Publisher', or 'Other' (a literary estate, a trust,
    -- anyone who isn't literally the book's author/publisher row).
    beneficiary_type        VARCHAR(20)  NOT NULL DEFAULT 'Other',
    beneficiary_email_id    VARCHAR(80),
    beneficiary_contact_no  VARCHAR(20),
    beneficiary_bank_name   VARCHAR(100),
    beneficiary_bank_branch VARCHAR(100),
    beneficiary_ifsc        VARCHAR(20),
    beneficiary_acc_no      VARCHAR(30),
    beneficiary_acc_type    VARCHAR(20),
    beneficiary_pan         VARCHAR(15)
);

-- ---------------------------------------------------------------------
-- 11. cart
-- ---------------------------------------------------------------------
-- UNIQUE(user_id, product_id) backs CartRepository.findByUserAndProduct,
-- which returns Optional and would blow up on duplicate rows.
-- See BACKEND_FIXES.md #F-03.
-- rent_days is NULL for a line the reader means to buy, and set to the
-- number of days for a line they mean to rent - each line carries its
-- own choice, so a buy and a rent can sit in the same cart at once
-- (see CartServiceImpl.addToCart / CheckoutService).
CREATE TABLE cart (
    cart_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    product_id INT NOT NULL,
    qty        INT NOT NULL,
    rent_days  INT NULL,

    CONSTRAINT fk_cart_user    FOREIGN KEY (user_id)    REFERENCES `user` (User_ID),
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES product (product_id),
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id)
);

-- ---------------------------------------------------------------------
-- 12. my_shelf
-- ---------------------------------------------------------------------
-- UNIQUE(user_id, product_id) backs
-- MyShelfRepository.existsByUser_UserIdAndProduct_ProductId.
--
-- My Shelf holds PURCHASED books only. There is no expiry column any
-- more, because a purchase never expires - rented books now live in
-- my_library instead, alongside library-package borrows (see below).
CREATE TABLE my_shelf (
    shelf_id            INT AUTO_INCREMENT PRIMARY KEY,
    user_id             INT NOT NULL,
    product_id          INT NOT NULL,

    CONSTRAINT fk_shelf_user    FOREIGN KEY (user_id)    REFERENCES `user` (User_ID),
    CONSTRAINT fk_shelf_product FOREIGN KEY (product_id) REFERENCES product (product_id),
    CONSTRAINT uq_shelf_user_product UNIQUE (user_id, product_id)
);

-- ---------------------------------------------------------------------
-- 13. library_package
-- ---------------------------------------------------------------------
-- UNIQUE(name) backs LibraryPackageRepository.findByName / existsByName.
CREATE TABLE library_package (
    package_id    INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(50)    NOT NULL UNIQUE,
    cost          DECIMAL(10, 2) NOT NULL,
    validity_days INT            NOT NULL,
    book_limit    INT            NOT NULL,
    description   VARCHAR(255)   NOT NULL
);

-- ---------------------------------------------------------------------
-- 14. transactions
-- ---------------------------------------------------------------------
-- The Transaction entity leaves most fields un-annotated, so Spring
-- Boot's default naming strategy snake-cases them. These names are what
-- Hibernate expects.
CREATE TABLE transactions (
    transaction_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT,
    total_amount     DECIMAL(12, 2),
    status           VARCHAR(20),
    created_at       DATETIME,
    transaction_type VARCHAR(10),

    CONSTRAINT fk_transaction_user FOREIGN KEY (user_id) REFERENCES `user` (User_ID)
);

CREATE TABLE transaction_items (
    item_id        INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT,
    product_id     INT,
    price          DECIMAL(10, 2),
    quantity       INT,

    CONSTRAINT fk_txitem_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (transaction_id),
    CONSTRAINT fk_txitem_product     FOREIGN KEY (product_id)     REFERENCES product (product_id)
);

-- ---------------------------------------------------------------------
-- 16. royalty_calculation
-- ---------------------------------------------------------------------
-- itemId -> item_id, Total_Royalty -> total_royalty, and the FK to
-- transaction_items is real (the original disabled it with
-- ConstraintMode.NO_CONSTRAINT). See BACKEND_FIXES.md #F-04.
CREATE TABLE royalty_calculation (
    roycal_id       INT AUTO_INCREMENT PRIMARY KEY,
    item_id         INT            NOT NULL,
    roycal_trandate DATE,
    product_id      INT            NOT NULL,
    total_amount    DECIMAL(12, 2),
    royalty_percent DECIMAL(5, 2),
    total_royalty   DECIMAL(12, 2),

    CONSTRAINT fk_roycal_item    FOREIGN KEY (item_id)    REFERENCES transaction_items (item_id),
    CONSTRAINT fk_roycal_product FOREIGN KEY (product_id) REFERENCES product (product_id)
);

CREATE TABLE product_beneficiary (
    prodben_id       INT AUTO_INCREMENT PRIMARY KEY,
    beneficiary_id   INT NOT NULL,
    product_id       INT NOT NULL,
    roycal_id        INT NOT NULL,
    royalty_received DECIMAL(12, 2),

    CONSTRAINT fk_prodben_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiary (beneficiary_id),
    CONSTRAINT fk_prodben_product     FOREIGN KEY (product_id)     REFERENCES product (product_id),
    CONSTRAINT fk_prodben_roycal      FOREIGN KEY (roycal_id)      REFERENCES royalty_calculation (roycal_id)
);

CREATE TABLE library_package_purchase (
    purchase_id    INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT         NOT NULL,
    user_id        INT            NOT NULL,
    package_id     INT            NOT NULL,
    package_price  DECIMAL(12, 2) NOT NULL,
    allowed_books  INT            NOT NULL,
    avg_book_price DECIMAL(12, 2) NOT NULL,
    purchase_date  DATETIME       NOT NULL,

    CONSTRAINT fk_purchase_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (transaction_id),
    CONSTRAINT fk_purchase_user        FOREIGN KEY (user_id)        REFERENCES `user` (User_ID),
    CONSTRAINT fk_purchase_package     FOREIGN KEY (package_id)     REFERENCES library_package (package_id)
);

CREATE TABLE library_package_purchase_item (
    item_id         INT AUTO_INCREMENT PRIMARY KEY,
    purchase_id     INT            NOT NULL,
    product_id      INT            NOT NULL,
    royalty_percent DECIMAL(5, 2)  NOT NULL,
    royalty_amount  DECIMAL(12, 2) NOT NULL,

    CONSTRAINT fk_purchitem_purchase FOREIGN KEY (purchase_id) REFERENCES library_package_purchase (purchase_id),
    CONSTRAINT fk_purchitem_product  FOREIGN KEY (product_id)  REFERENCES product (product_id)
);

-- ---------------------------------------------------------------------
-- 20. my_library
-- ---------------------------------------------------------------------
-- My Library holds RENTED books (paid per day, no package) and LENT
-- books (borrowed against a library package) side by side. access_type
-- tells the two apart.
--
-- package_id, books_allowed and books_taken are nullable because a
-- plain rental has no library package behind it at all - those three
-- columns only ever get a value on a LEND row.
CREATE TABLE my_library (
    my_lib_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    package_id    INT,
    product_id    INT,
    access_type   VARCHAR(10) NOT NULL,
    start_date    DATE,
    end_date      DATE,
    books_allowed INT,
    books_taken   INT,

    CONSTRAINT fk_mylib_user    FOREIGN KEY (user_id)    REFERENCES `user` (User_ID),
    CONSTRAINT fk_mylib_package FOREIGN KEY (package_id) REFERENCES library_package (package_id),
    CONSTRAINT fk_mylib_product FOREIGN KEY (product_id) REFERENCES product (product_id)
);

-- ---------------------------------------------------------------------
-- 21. pdf_book
-- ---------------------------------------------------------------------
-- Renamed from pdfBook (mixed case) to pdf_book. product_id is UNIQUE
-- because ReadBookRepository.findByProduct_ProductId returns a single
-- row. See BACKEND_FIXES.md #F-05.
CREATE TABLE pdf_book (
    pdf_Id     INT AUTO_INCREMENT PRIMARY KEY,
    pdf_data   LONGBLOB,
    file_name  VARCHAR(255),
    product_id INT NOT NULL UNIQUE,

    CONSTRAINT fk_pdfbook_product FOREIGN KEY (product_id) REFERENCES product (product_id)
);

-- ---------------------------------------------------------------------
-- 22. product_cover
-- ---------------------------------------------------------------------
-- The cover image behind a product, stored the same way pdf_book stores
-- its PDF: as bytes in the database, one row per product. product_id is
-- UNIQUE for the same reason as pdf_book - uploading a new cover
-- replaces the old row instead of adding a second one.
CREATE TABLE product_cover (
    cover_id     INT AUTO_INCREMENT PRIMARY KEY,
    image_data   LONGBLOB     NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    product_id   INT          NOT NULL UNIQUE,

    CONSTRAINT fk_cover_product FOREIGN KEY (product_id) REFERENCES product (product_id)
);

-- ---------------------------------------------------------------------
-- 23. beneficiary_assignment
-- ---------------------------------------------------------------------
-- Which beneficiaries currently receive royalty for which product -
-- a plain many-to-many join, separate from product_beneficiary (which
-- is payout HISTORY: one row per amount actually paid out at a past
-- sale, tied to a specific royalty_calculation). This table is current
-- configuration only, and has no money amounts of its own.
--
-- UNIQUE(product_id, beneficiary_id) - the same beneficiary can't be
-- assigned to the same book twice.
CREATE TABLE beneficiary_assignment (
    assignment_id  INT AUTO_INCREMENT PRIMARY KEY,
    product_id     INT NOT NULL,
    beneficiary_id INT NOT NULL,

    CONSTRAINT fk_benassign_product     FOREIGN KEY (product_id)     REFERENCES product (product_id),
    CONSTRAINT fk_benassign_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES beneficiary (beneficiary_id),
    CONSTRAINT uq_benassign UNIQUE (product_id, beneficiary_id)
);

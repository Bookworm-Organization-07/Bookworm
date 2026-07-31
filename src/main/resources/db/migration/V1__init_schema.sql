-- Bookworm.com schema (Rev.3 design, gift-related ORDERS columns dropped per product decision).
-- Engine/charset: InnoDB / utf8mb4 throughout.

CREATE TABLE product_categories (
    category_id     INT AUTO_INCREMENT PRIMARY KEY,
    category_name   VARCHAR(100) NOT NULL UNIQUE,
    icon_url        VARCHAR(255) NULL,
    display_order   INT NOT NULL DEFAULT 0,
    is_active       TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE languages (
    language_id     INT AUTO_INCREMENT PRIMARY KEY,
    language_code   VARCHAR(10) NOT NULL UNIQUE,
    language_name   VARCHAR(50) NOT NULL,
    is_default      TINYINT(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stakeholder_roles (
    stakeholder_role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name            VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE tags (
    tag_id    INT AUTO_INCREMENT PRIMARY KEY,
    tag_name  VARCHAR(60) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admins (
    admin_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    role           ENUM('SUPER_ADMIN','CONTENT_MANAGER') NOT NULL,
    status         ENUM('ACTIVE','DISABLED') NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE users (
    user_id               INT AUTO_INCREMENT PRIMARY KEY,
    full_name             VARCHAR(150) NOT NULL,
    email                 VARCHAR(150) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    phone                 VARCHAR(20) NULL,
    address               VARCHAR(255) NULL,
    occupation            VARCHAR(100) NULL,
    professional_domain   VARCHAR(100) NULL,
    user_type             ENUM('MEMBER','PARTNER') NOT NULL DEFAULT 'MEMBER',
    activation_token      VARCHAR(255) NULL,
    status                ENUM('PENDING','ACTIVE','DISABLED') NOT NULL DEFAULT 'PENDING',
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE genres (
    genre_id       INT AUTO_INCREMENT PRIMARY KEY,
    category_id    INT NOT NULL,
    genre_name     VARCHAR(100) NOT NULL,
    display_order  INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_genres_category FOREIGN KEY (category_id)
        REFERENCES product_categories (category_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE stakeholders (
    stakeholder_id           INT AUTO_INCREMENT PRIMARY KEY,
    full_name                VARCHAR(150) NOT NULL,
    birth_date               DATE NULL,
    contact_number           VARCHAR(20) NULL,
    email                    VARCHAR(150) NULL,
    bio                      TEXT NULL,
    payout_mode              ENUM('BANK_TRANSFER','CHEQUE','OTHER') NULL,
    payout_account_details   VARCHAR(255) NULL,
    created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE library_packages (
    library_package_id  INT AUTO_INCREMENT PRIMARY KEY,
    package_name         VARCHAR(100) NOT NULL,
    price                DECIMAL(10,2) NOT NULL,
    valid_days           INT NOT NULL,
    books_allowed        INT NOT NULL,
    is_active            TINYINT(1) NOT NULL DEFAULT 1,
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE products (
    product_id           INT AUTO_INCREMENT PRIMARY KEY,
    category_id          INT NOT NULL,
    genre_id             INT NOT NULL,
    language_id          INT NOT NULL,
    title                VARCHAR(255) NOT NULL,
    description          TEXT NULL,
    cover_image_url      VARCHAR(255) NULL,
    base_price           DECIMAL(10,2) NOT NULL,
    sale_price           DECIMAL(10,2) NOT NULL,
    pages                INT NULL,
    duration_minutes     INT NULL,
    main_file_url        VARCHAR(255) NULL,
    preview_file_url     VARCHAR(255) NULL,
    file_type            VARCHAR(20) NULL,
    rent_price_per_day   DECIMAL(10,2) NULL,
    is_rentable          TINYINT(1) NOT NULL DEFAULT 0,
    is_lendable          TINYINT(1) NOT NULL DEFAULT 0,
    is_featured          TINYINT(1) NOT NULL DEFAULT 0,
    is_bestseller        TINYINT(1) NOT NULL DEFAULT 0,
    is_active            TINYINT(1) NOT NULL DEFAULT 1,
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id)
        REFERENCES product_categories (category_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_products_genre FOREIGN KEY (genre_id)
        REFERENCES genres (genre_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_products_language FOREIGN KEY (language_id)
        REFERENCES languages (language_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_stakeholders (
    product_stakeholder_id  INT AUTO_INCREMENT PRIMARY KEY,
    product_id               INT NOT NULL,
    stakeholder_id           INT NOT NULL,
    stakeholder_role_id      INT NOT NULL,
    royalty_percentage       DECIMAL(5,2) NOT NULL,
    created_at               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prod_stake_product FOREIGN KEY (product_id)
        REFERENCES products (product_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_prod_stake_stakeholder FOREIGN KEY (stakeholder_id)
        REFERENCES stakeholders (stakeholder_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_prod_stake_role FOREIGN KEY (stakeholder_role_id)
        REFERENCES stakeholder_roles (stakeholder_role_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE product_tags (
    product_id  INT NOT NULL,
    tag_id      INT NOT NULL,
    PRIMARY KEY (product_id, tag_id),
    CONSTRAINT fk_product_tags_product FOREIGN KEY (product_id)
        REFERENCES products (product_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_product_tags_tag FOREIGN KEY (tag_id)
        REFERENCES tags (tag_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cart (
    cart_id     INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL UNIQUE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE cart_items (
    cart_item_id         INT AUTO_INCREMENT PRIMARY KEY,
    cart_id              INT NOT NULL,
    item_type            ENUM('PRODUCT','LIBRARY_PACKAGE') NOT NULL DEFAULT 'PRODUCT',
    product_id           INT NULL,
    library_package_id   INT NULL,
    access_type          ENUM('BUY','RENT','SUBSCRIBE') NOT NULL,
    rent_days            INT NULL,
    unit_price           DECIMAL(10,2) NOT NULL,
    quantity             INT NOT NULL DEFAULT 1,
    added_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id)
        REFERENCES cart (cart_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id)
        REFERENCES products (product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_cart_items_package FOREIGN KEY (library_package_id)
        REFERENCES library_packages (library_package_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_cart_items_item_type CHECK (
        (item_type = 'PRODUCT' AND product_id IS NOT NULL AND library_package_id IS NULL)
        OR (item_type = 'LIBRARY_PACKAGE' AND library_package_id IS NOT NULL AND product_id IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
    order_id                INT AUTO_INCREMENT PRIMARY KEY,
    user_id                 INT NOT NULL,
    subtotal_amount         DECIMAL(10,2) NOT NULL,
    discount_amount         DECIMAL(10,2) NOT NULL DEFAULT 0,
    vat_amount              DECIMAL(10,2) NOT NULL DEFAULT 0,
    service_charge_amount   DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_payable_amount    DECIMAL(10,2) NOT NULL,
    payment_mode            ENUM('CREDIT_CARD','NET_BANKING') NOT NULL,
    order_status            ENUM('PENDING','PAID','CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_items (
    order_item_id        INT AUTO_INCREMENT PRIMARY KEY,
    order_id             INT NOT NULL,
    item_type            ENUM('PRODUCT','LIBRARY_PACKAGE') NOT NULL DEFAULT 'PRODUCT',
    product_id           INT NULL,
    library_package_id   INT NULL,
    access_type          ENUM('BUY','RENT','SUBSCRIBE') NOT NULL,
    rent_days            INT NULL,
    valid_from           DATE NULL,
    valid_to             DATE NULL,
    unit_price           DECIMAL(10,2) NOT NULL,
    quantity             INT NOT NULL DEFAULT 1,
    created_at           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id)
        REFERENCES orders (order_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id)
        REFERENCES products (product_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_order_items_package FOREIGN KEY (library_package_id)
        REFERENCES library_packages (library_package_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT chk_order_items_item_type CHECK (
        (item_type = 'PRODUCT' AND product_id IS NOT NULL AND library_package_id IS NULL)
        OR (item_type = 'LIBRARY_PACKAGE' AND library_package_id IS NOT NULL AND product_id IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payments (
    payment_id          INT AUTO_INCREMENT PRIMARY KEY,
    order_id            INT NOT NULL,
    amount_attempted    DECIMAL(10,2) NOT NULL,
    payment_mode        ENUM('CREDIT_CARD','NET_BANKING') NOT NULL,
    gateway_reference   VARCHAR(150) NULL,
    payment_status      ENUM('INITIATED','SUCCESS','FAILED') NOT NULL DEFAULT 'INITIATED',
    payment_date        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id)
        REFERENCES orders (order_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE shelf (
    shelf_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    product_id      INT NOT NULL,
    order_item_id   INT NOT NULL UNIQUE,
    acquired_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shelf_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_shelf_product FOREIGN KEY (product_id)
        REFERENCES products (product_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_shelf_order_item FOREIGN KEY (order_item_id)
        REFERENCES order_items (order_item_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_library_memberships (
    membership_id        INT AUTO_INCREMENT PRIMARY KEY,
    user_id               INT NOT NULL,
    library_package_id    INT NOT NULL,
    order_item_id         INT NOT NULL UNIQUE,
    books_used            INT NOT NULL DEFAULT 0,
    start_date            DATE NOT NULL,
    expiry_date           DATE NOT NULL,
    status                ENUM('ACTIVE','EXPIRED','VOID') NOT NULL DEFAULT 'ACTIVE',
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ulm_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_ulm_package FOREIGN KEY (library_package_id)
        REFERENCES library_packages (library_package_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_ulm_order_item FOREIGN KEY (order_item_id)
        REFERENCES order_items (order_item_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Named library_loans, not "library": LIBRARY is a reserved word on the MySQL version in use here.
CREATE TABLE library_loans (
    library_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT NOT NULL,
    product_id       INT NOT NULL,
    access_type      ENUM('RENTED','LENT') NOT NULL,
    membership_id    INT NULL,
    order_item_id    INT NULL UNIQUE,
    start_date       DATETIME NOT NULL,
    expiry_date      DATETIME NOT NULL,
    status           ENUM('ACTIVE','RETURNED','EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_library_user FOREIGN KEY (user_id)
        REFERENCES users (user_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_library_product FOREIGN KEY (product_id)
        REFERENCES products (product_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_library_membership FOREIGN KEY (membership_id)
        REFERENCES user_library_memberships (membership_id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_library_order_item FOREIGN KEY (order_item_id)
        REFERENCES order_items (order_item_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE royalty_transactions (
    royalty_transaction_id  INT AUTO_INCREMENT PRIMARY KEY,
    product_stakeholder_id   INT NOT NULL,
    source_type              ENUM('PURCHASE','RENT','LIBRARY_LEND') NOT NULL,
    source_reference_id      INT NOT NULL,
    royalty_base_amount      DECIMAL(10,2) NOT NULL,
    royalty_amount           DECIMAL(10,2) NOT NULL,
    payout_status            ENUM('PENDING','PAID') NOT NULL DEFAULT 'PENDING',
    payout_date              DATETIME NULL,
    transaction_date         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_royalty_product_stakeholder FOREIGN KEY (product_stakeholder_id)
        REFERENCES product_stakeholders (product_stakeholder_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE upload_batches (
    upload_batch_id  INT AUTO_INCREMENT PRIMARY KEY,
    admin_id          INT NOT NULL,
    file_name         VARCHAR(255) NOT NULL,
    total_rows        INT NOT NULL DEFAULT 0,
    success_rows      INT NOT NULL DEFAULT 0,
    failed_rows       INT NOT NULL DEFAULT 0,
    error_log         TEXT NULL,
    status            ENUM('PROCESSING','COMPLETED','FAILED') NOT NULL DEFAULT 'PROCESSING',
    uploaded_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_upload_batches_admin FOREIGN KEY (admin_id)
        REFERENCES admins (admin_id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE app_config (
    config_id     INT AUTO_INCREMENT PRIMARY KEY,
    config_key    VARCHAR(100) NOT NULL UNIQUE,
    config_value  TEXT NOT NULL,
    description   VARCHAR(255) NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

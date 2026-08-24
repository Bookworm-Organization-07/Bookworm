using System;
using Microsoft.EntityFrameworkCore.Metadata;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace BookwormApi.Migrations
{
    /// <inheritdoc />
    public partial class InitialCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterDatabase()
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "attribute",
                columns: table => new
                {
                    Attribute_Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    Attribute_Desc = table.Column<string>(type: "varchar(255)", maxLength: 255, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_attribute", x => x.Attribute_Id);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "author",
                columns: table => new
                {
                    Author_ID = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    Name = table.Column<string>(type: "varchar(100)", maxLength: 100, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    Bio = table.Column<string>(type: "longtext", nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_author", x => x.Author_ID);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "beneficiary",
                columns: table => new
                {
                    beneficiary_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    beneficiary_name = table.Column<string>(type: "varchar(100)", maxLength: 100, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    beneficiary_type = table.Column<string>(type: "varchar(20)", maxLength: 20, nullable: false, defaultValue: "Other")
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    beneficiary_email_id = table.Column<string>(type: "varchar(80)", maxLength: 80, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    beneficiary_contact_no = table.Column<string>(type: "varchar(20)", maxLength: 20, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    beneficiary_bank_name = table.Column<string>(type: "varchar(100)", maxLength: 100, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    beneficiary_bank_branch = table.Column<string>(type: "varchar(100)", maxLength: 100, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    beneficiary_ifsc = table.Column<string>(type: "varchar(20)", maxLength: 20, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    beneficiary_acc_no = table.Column<string>(type: "varchar(30)", maxLength: 30, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    beneficiary_acc_type = table.Column<string>(type: "varchar(20)", maxLength: 20, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    beneficiary_pan = table.Column<string>(type: "varchar(15)", maxLength: 15, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_beneficiary", x => x.beneficiary_id);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "genere",
                columns: table => new
                {
                    Genere_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    Genere_Desc = table.Column<string>(type: "varchar(50)", maxLength: 50, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_genere", x => x.Genere_id);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "language",
                columns: table => new
                {
                    Language_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    Language_Desc = table.Column<string>(type: "varchar(50)", maxLength: 50, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_language", x => x.Language_id);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "library_package",
                columns: table => new
                {
                    package_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    name = table.Column<string>(type: "varchar(50)", maxLength: 50, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    cost = table.Column<decimal>(type: "decimal(10,2)", precision: 10, scale: 2, nullable: false),
                    validity_days = table.Column<int>(type: "int", nullable: false),
                    book_limit = table.Column<int>(type: "int", nullable: false),
                    description = table.Column<string>(type: "varchar(255)", maxLength: 255, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_library_package", x => x.package_id);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "product_type_master",
                columns: table => new
                {
                    Type_Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    Type_Desc = table.Column<string>(type: "varchar(50)", maxLength: 50, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_product_type_master", x => x.Type_Id);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "publisher",
                columns: table => new
                {
                    Publisher_ID = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    Name = table.Column<string>(type: "varchar(100)", maxLength: 100, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    Email = table.Column<string>(type: "varchar(80)", maxLength: 80, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_publisher", x => x.Publisher_ID);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "user",
                columns: table => new
                {
                    User_ID = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    User_Name = table.Column<string>(type: "varchar(80)", maxLength: 80, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    User_Email = table.Column<string>(type: "varchar(80)", maxLength: 80, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    User_Phone = table.Column<string>(type: "varchar(20)", maxLength: 20, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    User_Address = table.Column<string>(type: "longtext", nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    User_Password = table.Column<string>(type: "varchar(255)", maxLength: 255, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    is_Admin = table.Column<bool>(type: "tinyint(1)", nullable: false),
                    Join_Date = table.Column<DateOnly>(type: "date", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_user", x => x.User_ID);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "product",
                columns: table => new
                {
                    product_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    product_name = table.Column<string>(type: "varchar(150)", maxLength: 150, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    product_name_english = table.Column<string>(type: "varchar(150)", maxLength: 150, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    product_type = table.Column<int>(type: "int", nullable: true),
                    product_author = table.Column<int>(type: "int", nullable: true),
                    product_publisher = table.Column<int>(type: "int", nullable: true),
                    product_lang = table.Column<int>(type: "int", nullable: true),
                    product_genere = table.Column<int>(type: "int", nullable: true),
                    product_baseprice = table.Column<decimal>(type: "decimal(10,2)", precision: 10, scale: 2, nullable: false),
                    product_offerprice = table.Column<decimal>(type: "decimal(10,2)", precision: 10, scale: 2, nullable: true),
                    discount_percent = table.Column<decimal>(type: "decimal(5,2)", precision: 5, scale: 2, nullable: true),
                    royalty_percent = table.Column<decimal>(type: "decimal(5,2)", precision: 5, scale: 2, nullable: true),
                    product_off_price_expirydate = table.Column<DateOnly>(type: "date", nullable: true),
                    product_description_short = table.Column<string>(type: "varchar(255)", maxLength: 255, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    product_description_long = table.Column<string>(type: "longtext", nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    product_isbn = table.Column<string>(type: "varchar(20)", maxLength: 20, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    is_rentable = table.Column<bool>(type: "tinyint(1)", nullable: false),
                    is_library = table.Column<bool>(type: "tinyint(1)", nullable: false),
                    rent_per_day = table.Column<decimal>(type: "decimal(5,2)", precision: 5, scale: 2, nullable: true),
                    min_rent_days = table.Column<int>(type: "int", nullable: false),
                    product_Image = table.Column<string>(type: "varchar(255)", maxLength: 255, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_product", x => x.product_id);
                    table.ForeignKey(
                        name: "fk_product_author",
                        column: x => x.product_author,
                        principalTable: "author",
                        principalColumn: "Author_ID");
                    table.ForeignKey(
                        name: "fk_product_genere",
                        column: x => x.product_genere,
                        principalTable: "genere",
                        principalColumn: "Genere_id");
                    table.ForeignKey(
                        name: "fk_product_lang",
                        column: x => x.product_lang,
                        principalTable: "language",
                        principalColumn: "Language_id");
                    table.ForeignKey(
                        name: "fk_product_publisher",
                        column: x => x.product_publisher,
                        principalTable: "publisher",
                        principalColumn: "Publisher_ID");
                    table.ForeignKey(
                        name: "fk_product_type",
                        column: x => x.product_type,
                        principalTable: "product_type_master",
                        principalColumn: "Type_Id");
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "transactions",
                columns: table => new
                {
                    transaction_id = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    user_id = table.Column<int>(type: "int", nullable: true),
                    total_amount = table.Column<decimal>(type: "decimal(12,2)", precision: 12, scale: 2, nullable: true),
                    status = table.Column<string>(type: "varchar(20)", maxLength: 20, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    created_at = table.Column<DateTime>(type: "datetime(6)", nullable: true),
                    transaction_type = table.Column<string>(type: "varchar(10)", maxLength: 10, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_transactions", x => x.transaction_id);
                    table.ForeignKey(
                        name: "fk_transaction_user",
                        column: x => x.user_id,
                        principalTable: "user",
                        principalColumn: "User_ID");
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "beneficiary_assignment",
                columns: table => new
                {
                    assignment_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    product_id = table.Column<int>(type: "int", nullable: false),
                    beneficiary_id = table.Column<int>(type: "int", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_beneficiary_assignment", x => x.assignment_id);
                    table.ForeignKey(
                        name: "fk_benassign_beneficiary",
                        column: x => x.beneficiary_id,
                        principalTable: "beneficiary",
                        principalColumn: "beneficiary_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_benassign_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "cart",
                columns: table => new
                {
                    cart_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    user_id = table.Column<int>(type: "int", nullable: false),
                    product_id = table.Column<int>(type: "int", nullable: false),
                    qty = table.Column<int>(type: "int", nullable: false),
                    rent_days = table.Column<int>(type: "int", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_cart", x => x.cart_id);
                    table.ForeignKey(
                        name: "fk_cart_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_cart_user",
                        column: x => x.user_id,
                        principalTable: "user",
                        principalColumn: "User_ID",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "my_library",
                columns: table => new
                {
                    my_lib_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    user_id = table.Column<int>(type: "int", nullable: false),
                    package_id = table.Column<int>(type: "int", nullable: true),
                    product_id = table.Column<int>(type: "int", nullable: true),
                    access_type = table.Column<string>(type: "varchar(10)", maxLength: 10, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    start_date = table.Column<DateOnly>(type: "date", nullable: true),
                    end_date = table.Column<DateOnly>(type: "date", nullable: true),
                    books_allowed = table.Column<int>(type: "int", nullable: true),
                    books_taken = table.Column<int>(type: "int", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_my_library", x => x.my_lib_id);
                    table.ForeignKey(
                        name: "fk_mylib_package",
                        column: x => x.package_id,
                        principalTable: "library_package",
                        principalColumn: "package_id");
                    table.ForeignKey(
                        name: "fk_mylib_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id");
                    table.ForeignKey(
                        name: "fk_mylib_user",
                        column: x => x.user_id,
                        principalTable: "user",
                        principalColumn: "User_ID",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "my_shelf",
                columns: table => new
                {
                    shelf_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    user_id = table.Column<int>(type: "int", nullable: false),
                    product_id = table.Column<int>(type: "int", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_my_shelf", x => x.shelf_id);
                    table.ForeignKey(
                        name: "fk_shelf_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_shelf_user",
                        column: x => x.user_id,
                        principalTable: "user",
                        principalColumn: "User_ID",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "pdf_book",
                columns: table => new
                {
                    pdf_Id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    pdf_data = table.Column<byte[]>(type: "LONGBLOB", nullable: true),
                    file_name = table.Column<string>(type: "varchar(255)", maxLength: 255, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    product_id = table.Column<int>(type: "int", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_pdf_book", x => x.pdf_Id);
                    table.ForeignKey(
                        name: "fk_pdfbook_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "product_attribute",
                columns: table => new
                {
                    prod_att_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    product_id = table.Column<int>(type: "int", nullable: false),
                    attribute_id = table.Column<int>(type: "int", nullable: false),
                    attribute_value = table.Column<string>(type: "varchar(255)", maxLength: 255, nullable: true)
                        .Annotation("MySql:CharSet", "utf8mb4")
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_product_attribute", x => x.prod_att_id);
                    table.ForeignKey(
                        name: "fk_prodattr_attribute",
                        column: x => x.attribute_id,
                        principalTable: "attribute",
                        principalColumn: "Attribute_Id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_prodattr_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "product_cover",
                columns: table => new
                {
                    cover_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    image_data = table.Column<byte[]>(type: "LONGBLOB", nullable: false),
                    content_type = table.Column<string>(type: "varchar(100)", maxLength: 100, nullable: false)
                        .Annotation("MySql:CharSet", "utf8mb4"),
                    product_id = table.Column<int>(type: "int", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_product_cover", x => x.cover_id);
                    table.ForeignKey(
                        name: "fk_cover_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "library_package_purchase",
                columns: table => new
                {
                    purchase_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    transaction_id = table.Column<long>(type: "bigint", nullable: false),
                    user_id = table.Column<int>(type: "int", nullable: false),
                    package_id = table.Column<int>(type: "int", nullable: false),
                    package_price = table.Column<decimal>(type: "decimal(12,2)", precision: 12, scale: 2, nullable: false),
                    allowed_books = table.Column<int>(type: "int", nullable: false),
                    avg_book_price = table.Column<decimal>(type: "decimal(12,2)", precision: 12, scale: 2, nullable: false),
                    purchase_date = table.Column<DateTime>(type: "datetime(6)", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_library_package_purchase", x => x.purchase_id);
                    table.ForeignKey(
                        name: "fk_purchase_package",
                        column: x => x.package_id,
                        principalTable: "library_package",
                        principalColumn: "package_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_purchase_transaction",
                        column: x => x.transaction_id,
                        principalTable: "transactions",
                        principalColumn: "transaction_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_purchase_user",
                        column: x => x.user_id,
                        principalTable: "user",
                        principalColumn: "User_ID",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "transaction_items",
                columns: table => new
                {
                    item_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    transaction_id = table.Column<long>(type: "bigint", nullable: true),
                    product_id = table.Column<int>(type: "int", nullable: true),
                    price = table.Column<decimal>(type: "decimal(10,2)", precision: 10, scale: 2, nullable: true),
                    quantity = table.Column<int>(type: "int", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_transaction_items", x => x.item_id);
                    table.ForeignKey(
                        name: "fk_txitem_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id");
                    table.ForeignKey(
                        name: "fk_txitem_transaction",
                        column: x => x.transaction_id,
                        principalTable: "transactions",
                        principalColumn: "transaction_id");
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "library_package_purchase_item",
                columns: table => new
                {
                    item_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    purchase_id = table.Column<int>(type: "int", nullable: false),
                    product_id = table.Column<int>(type: "int", nullable: false),
                    royalty_percent = table.Column<decimal>(type: "decimal(5,2)", precision: 5, scale: 2, nullable: false),
                    royalty_amount = table.Column<decimal>(type: "decimal(12,2)", precision: 12, scale: 2, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_library_package_purchase_item", x => x.item_id);
                    table.ForeignKey(
                        name: "fk_purchitem_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_purchitem_purchase",
                        column: x => x.purchase_id,
                        principalTable: "library_package_purchase",
                        principalColumn: "purchase_id",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "royalty_calculation",
                columns: table => new
                {
                    roycal_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    item_id = table.Column<int>(type: "int", nullable: false),
                    roycal_trandate = table.Column<DateOnly>(type: "date", nullable: true),
                    product_id = table.Column<int>(type: "int", nullable: false),
                    total_amount = table.Column<decimal>(type: "decimal(12,2)", precision: 12, scale: 2, nullable: true),
                    royalty_percent = table.Column<decimal>(type: "decimal(5,2)", precision: 5, scale: 2, nullable: true),
                    total_royalty = table.Column<decimal>(type: "decimal(12,2)", precision: 12, scale: 2, nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_royalty_calculation", x => x.roycal_id);
                    table.ForeignKey(
                        name: "fk_roycal_item",
                        column: x => x.item_id,
                        principalTable: "transaction_items",
                        principalColumn: "item_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_roycal_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateTable(
                name: "product_beneficiary",
                columns: table => new
                {
                    prodben_id = table.Column<int>(type: "int", nullable: false)
                        .Annotation("MySql:ValueGenerationStrategy", MySqlValueGenerationStrategy.IdentityColumn),
                    beneficiary_id = table.Column<int>(type: "int", nullable: false),
                    product_id = table.Column<int>(type: "int", nullable: false),
                    roycal_id = table.Column<int>(type: "int", nullable: false),
                    royalty_received = table.Column<decimal>(type: "decimal(12,2)", precision: 12, scale: 2, nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_product_beneficiary", x => x.prodben_id);
                    table.ForeignKey(
                        name: "fk_prodben_beneficiary",
                        column: x => x.beneficiary_id,
                        principalTable: "beneficiary",
                        principalColumn: "beneficiary_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_prodben_product",
                        column: x => x.product_id,
                        principalTable: "product",
                        principalColumn: "product_id",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "fk_prodben_roycal",
                        column: x => x.roycal_id,
                        principalTable: "royalty_calculation",
                        principalColumn: "roycal_id",
                        onDelete: ReferentialAction.Cascade);
                })
                .Annotation("MySql:CharSet", "utf8mb4");

            migrationBuilder.CreateIndex(
                name: "IX_beneficiary_assignment_beneficiary_id",
                table: "beneficiary_assignment",
                column: "beneficiary_id");

            migrationBuilder.CreateIndex(
                name: "IX_beneficiary_assignment_product_id_beneficiary_id",
                table: "beneficiary_assignment",
                columns: new[] { "product_id", "beneficiary_id" },
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_cart_product_id",
                table: "cart",
                column: "product_id");

            migrationBuilder.CreateIndex(
                name: "IX_cart_user_id_product_id",
                table: "cart",
                columns: new[] { "user_id", "product_id" },
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_genere_Genere_Desc",
                table: "genere",
                column: "Genere_Desc",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_language_Language_Desc",
                table: "language",
                column: "Language_Desc",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_library_package_name",
                table: "library_package",
                column: "name",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_library_package_purchase_package_id",
                table: "library_package_purchase",
                column: "package_id");

            migrationBuilder.CreateIndex(
                name: "IX_library_package_purchase_transaction_id",
                table: "library_package_purchase",
                column: "transaction_id");

            migrationBuilder.CreateIndex(
                name: "IX_library_package_purchase_user_id",
                table: "library_package_purchase",
                column: "user_id");

            migrationBuilder.CreateIndex(
                name: "IX_library_package_purchase_item_product_id",
                table: "library_package_purchase_item",
                column: "product_id");

            migrationBuilder.CreateIndex(
                name: "IX_library_package_purchase_item_purchase_id",
                table: "library_package_purchase_item",
                column: "purchase_id");

            migrationBuilder.CreateIndex(
                name: "IX_my_library_package_id",
                table: "my_library",
                column: "package_id");

            migrationBuilder.CreateIndex(
                name: "IX_my_library_product_id",
                table: "my_library",
                column: "product_id");

            migrationBuilder.CreateIndex(
                name: "IX_my_library_user_id",
                table: "my_library",
                column: "user_id");

            migrationBuilder.CreateIndex(
                name: "IX_my_shelf_product_id",
                table: "my_shelf",
                column: "product_id");

            migrationBuilder.CreateIndex(
                name: "IX_my_shelf_user_id_product_id",
                table: "my_shelf",
                columns: new[] { "user_id", "product_id" },
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_pdf_book_product_id",
                table: "pdf_book",
                column: "product_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_product_product_author",
                table: "product",
                column: "product_author");

            migrationBuilder.CreateIndex(
                name: "IX_product_product_genere",
                table: "product",
                column: "product_genere");

            migrationBuilder.CreateIndex(
                name: "IX_product_product_isbn",
                table: "product",
                column: "product_isbn",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_product_product_lang",
                table: "product",
                column: "product_lang");

            migrationBuilder.CreateIndex(
                name: "IX_product_product_publisher",
                table: "product",
                column: "product_publisher");

            migrationBuilder.CreateIndex(
                name: "IX_product_product_type",
                table: "product",
                column: "product_type");

            migrationBuilder.CreateIndex(
                name: "IX_product_attribute_attribute_id",
                table: "product_attribute",
                column: "attribute_id");

            migrationBuilder.CreateIndex(
                name: "IX_product_attribute_product_id_attribute_id",
                table: "product_attribute",
                columns: new[] { "product_id", "attribute_id" },
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_product_beneficiary_beneficiary_id",
                table: "product_beneficiary",
                column: "beneficiary_id");

            migrationBuilder.CreateIndex(
                name: "IX_product_beneficiary_product_id",
                table: "product_beneficiary",
                column: "product_id");

            migrationBuilder.CreateIndex(
                name: "IX_product_beneficiary_roycal_id",
                table: "product_beneficiary",
                column: "roycal_id");

            migrationBuilder.CreateIndex(
                name: "IX_product_cover_product_id",
                table: "product_cover",
                column: "product_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_product_type_master_Type_Desc",
                table: "product_type_master",
                column: "Type_Desc",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_publisher_Email",
                table: "publisher",
                column: "Email",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "IX_royalty_calculation_item_id",
                table: "royalty_calculation",
                column: "item_id");

            migrationBuilder.CreateIndex(
                name: "IX_royalty_calculation_product_id",
                table: "royalty_calculation",
                column: "product_id");

            migrationBuilder.CreateIndex(
                name: "IX_transaction_items_product_id",
                table: "transaction_items",
                column: "product_id");

            migrationBuilder.CreateIndex(
                name: "IX_transaction_items_transaction_id",
                table: "transaction_items",
                column: "transaction_id");

            migrationBuilder.CreateIndex(
                name: "IX_transactions_user_id",
                table: "transactions",
                column: "user_id");

            migrationBuilder.CreateIndex(
                name: "IX_user_User_Email",
                table: "user",
                column: "User_Email",
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "beneficiary_assignment");

            migrationBuilder.DropTable(
                name: "cart");

            migrationBuilder.DropTable(
                name: "library_package_purchase_item");

            migrationBuilder.DropTable(
                name: "my_library");

            migrationBuilder.DropTable(
                name: "my_shelf");

            migrationBuilder.DropTable(
                name: "pdf_book");

            migrationBuilder.DropTable(
                name: "product_attribute");

            migrationBuilder.DropTable(
                name: "product_beneficiary");

            migrationBuilder.DropTable(
                name: "product_cover");

            migrationBuilder.DropTable(
                name: "library_package_purchase");

            migrationBuilder.DropTable(
                name: "attribute");

            migrationBuilder.DropTable(
                name: "beneficiary");

            migrationBuilder.DropTable(
                name: "royalty_calculation");

            migrationBuilder.DropTable(
                name: "library_package");

            migrationBuilder.DropTable(
                name: "transaction_items");

            migrationBuilder.DropTable(
                name: "product");

            migrationBuilder.DropTable(
                name: "transactions");

            migrationBuilder.DropTable(
                name: "author");

            migrationBuilder.DropTable(
                name: "genere");

            migrationBuilder.DropTable(
                name: "language");

            migrationBuilder.DropTable(
                name: "publisher");

            migrationBuilder.DropTable(
                name: "product_type_master");

            migrationBuilder.DropTable(
                name: "user");
        }
    }
}

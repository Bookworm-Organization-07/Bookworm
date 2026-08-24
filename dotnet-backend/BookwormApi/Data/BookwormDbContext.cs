using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Data;

public class BookwormDbContext : DbContext
{
    public BookwormDbContext(DbContextOptions<BookwormDbContext> options) : base(options)
    {
    }

    public DbSet<User> Users => Set<User>();
    public DbSet<Author> Authors => Set<Author>();
    public DbSet<Publisher> Publishers => Set<Publisher>();
    public DbSet<Language> Languages => Set<Language>();
    public DbSet<Genere> Generes => Set<Genere>();
    public DbSet<ProductType> ProductTypes => Set<ProductType>();
    public DbSet<AttributeEntity> Attributes => Set<AttributeEntity>();
    public DbSet<Product> Products => Set<Product>();
    public DbSet<ProductAttribute> ProductAttributes => Set<ProductAttribute>();
    public DbSet<Beneficiary> Beneficiaries => Set<Beneficiary>();
    public DbSet<BeneficiaryAssignment> BeneficiaryAssignments => Set<BeneficiaryAssignment>();
    public DbSet<Cart> Carts => Set<Cart>();
    public DbSet<MyShelf> MyShelves => Set<MyShelf>();
    public DbSet<LibraryPackage> LibraryPackages => Set<LibraryPackage>();
    public DbSet<Transaction> Transactions => Set<Transaction>();
    public DbSet<TransactionItem> TransactionItems => Set<TransactionItem>();
    public DbSet<RoyaltyCalculation> RoyaltyCalculations => Set<RoyaltyCalculation>();
    public DbSet<ProductBeneficiary> ProductBeneficiaries => Set<ProductBeneficiary>();
    public DbSet<LibraryPackagePurchase> LibraryPackagePurchases => Set<LibraryPackagePurchase>();
    public DbSet<LibraryPackagePurchaseItem> LibraryPackagePurchaseItems => Set<LibraryPackagePurchaseItem>();
    public DbSet<MyLibrary> MyLibraries => Set<MyLibrary>();
    public DbSet<ReadBook> ReadBooks => Set<ReadBook>();
    public DbSet<ProductCover> ProductCovers => Set<ProductCover>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<User>(e =>
        {
            e.ToTable("user");
            e.HasKey(x => x.UserId);
            e.Property(x => x.UserId).HasColumnName("User_ID");
            e.Property(x => x.UserName).HasColumnName("User_Name").HasMaxLength(80).IsRequired();
            e.Property(x => x.UserEmail).HasColumnName("User_Email").HasMaxLength(80).IsRequired();
            e.HasIndex(x => x.UserEmail).IsUnique();
            e.Property(x => x.UserPhone).HasColumnName("User_Phone").HasMaxLength(20);
            e.Property(x => x.UserAddress).HasColumnName("User_Address");
            e.Property(x => x.UserPassword).HasColumnName("User_Password").HasMaxLength(255).IsRequired();
            e.Property(x => x.Admin).HasColumnName("is_Admin").IsRequired();
            e.Property(x => x.JoinDate).HasColumnName("Join_Date");
        });

        modelBuilder.Entity<Author>(e =>
        {
            e.ToTable("author");
            e.HasKey(x => x.AuthorId);
            e.Property(x => x.AuthorId).HasColumnName("Author_ID");
            e.Property(x => x.Name).HasColumnName("Name").HasMaxLength(100).IsRequired();
            e.Property(x => x.Bio).HasColumnName("Bio");
        });

        modelBuilder.Entity<Publisher>(e =>
        {
            e.ToTable("publisher");
            e.HasKey(x => x.PublisherId);
            e.Property(x => x.PublisherId).HasColumnName("Publisher_ID");
            e.Property(x => x.Name).HasColumnName("Name").HasMaxLength(100).IsRequired();
            e.Property(x => x.Email).HasColumnName("Email").HasMaxLength(80).IsRequired();
            e.HasIndex(x => x.Email).IsUnique();
        });

        modelBuilder.Entity<Language>(e =>
        {
            e.ToTable("language");
            e.HasKey(x => x.LanguageId);
            e.Property(x => x.LanguageId).HasColumnName("Language_id");
            e.Property(x => x.LanguageDesc).HasColumnName("Language_Desc").HasMaxLength(50).IsRequired();
            e.HasIndex(x => x.LanguageDesc).IsUnique();
        });

        modelBuilder.Entity<Genere>(e =>
        {
            e.ToTable("genere");
            e.HasKey(x => x.GenereId);
            e.Property(x => x.GenereId).HasColumnName("Genere_id");
            e.Property(x => x.GenereDesc).HasColumnName("Genere_Desc").HasMaxLength(50).IsRequired();
            e.HasIndex(x => x.GenereDesc).IsUnique();
        });

        modelBuilder.Entity<ProductType>(e =>
        {
            e.ToTable("product_type_master");
            e.HasKey(x => x.TypeId);
            e.Property(x => x.TypeId).HasColumnName("Type_Id");
            e.Property(x => x.TypeDesc).HasColumnName("Type_Desc").HasMaxLength(50).IsRequired();
            e.HasIndex(x => x.TypeDesc).IsUnique();
        });

        modelBuilder.Entity<AttributeEntity>(e =>
        {
            e.ToTable("attribute");
            e.HasKey(x => x.AttributeId);
            e.Property(x => x.AttributeId).HasColumnName("Attribute_Id");
            e.Property(x => x.AttributeDesc).HasColumnName("Attribute_Desc").HasMaxLength(255).IsRequired();
        });

        modelBuilder.Entity<Product>(e =>
        {
            e.ToTable("product");
            e.HasKey(x => x.ProductId);
            e.Property(x => x.ProductId).HasColumnName("product_id");
            e.Property(x => x.ProductName).HasColumnName("product_name").HasMaxLength(150).IsRequired();
            e.Property(x => x.ProductNameEnglish).HasColumnName("product_name_english").HasMaxLength(150);
            e.Property(x => x.ProductTypeId).HasColumnName("product_type");
            e.Property(x => x.AuthorId).HasColumnName("product_author");
            e.Property(x => x.PublisherId).HasColumnName("product_publisher");
            e.Property(x => x.LanguageId).HasColumnName("product_lang");
            e.Property(x => x.GenereId).HasColumnName("product_genere");
            e.Property(x => x.ProductBaseprice).HasColumnName("product_baseprice").HasPrecision(10, 2).IsRequired();
            e.Property(x => x.ProductOfferprice).HasColumnName("product_offerprice").HasPrecision(10, 2);
            e.Property(x => x.DiscountPercent).HasColumnName("discount_percent").HasPrecision(5, 2);
            e.Property(x => x.RoyaltyPercent).HasColumnName("royalty_percent").HasPrecision(5, 2);
            e.Property(x => x.ProductOffPriceExpirydate).HasColumnName("product_off_price_expirydate");
            e.Property(x => x.ProductDescriptionShort).HasColumnName("product_description_short").HasMaxLength(255);
            e.Property(x => x.ProductDescriptionLong).HasColumnName("product_description_long");
            e.Property(x => x.ProductIsbn).HasColumnName("product_isbn").HasMaxLength(20).IsRequired();
            e.HasIndex(x => x.ProductIsbn).IsUnique();
            e.Property(x => x.Rentable).HasColumnName("is_rentable").IsRequired();
            e.Property(x => x.Library).HasColumnName("is_library").IsRequired();
            e.Property(x => x.RentPerDay).HasColumnName("rent_per_day").HasPrecision(5, 2);
            e.Property(x => x.MinRentDays).HasColumnName("min_rent_days").IsRequired();
            e.Property(x => x.ProductImage).HasColumnName("product_Image").HasMaxLength(255);

            e.HasOne(x => x.ProductType).WithMany().HasForeignKey(x => x.ProductTypeId).HasConstraintName("fk_product_type");
            e.HasOne(x => x.Author).WithMany().HasForeignKey(x => x.AuthorId).HasConstraintName("fk_product_author");
            e.HasOne(x => x.Publisher).WithMany().HasForeignKey(x => x.PublisherId).HasConstraintName("fk_product_publisher");
            e.HasOne(x => x.Language).WithMany().HasForeignKey(x => x.LanguageId).HasConstraintName("fk_product_lang");
            e.HasOne(x => x.Genere).WithMany().HasForeignKey(x => x.GenereId).HasConstraintName("fk_product_genere");
        });

        modelBuilder.Entity<ProductAttribute>(e =>
        {
            e.ToTable("product_attribute");
            e.HasKey(x => x.ProdAttId);
            e.Property(x => x.ProdAttId).HasColumnName("prod_att_id");
            e.Property(x => x.ProductId).HasColumnName("product_id").IsRequired();
            e.Property(x => x.AttributeId).HasColumnName("attribute_id").IsRequired();
            e.Property(x => x.AttributeValue).HasColumnName("attribute_value").HasMaxLength(255);
            e.HasIndex(x => new { x.ProductId, x.AttributeId }).IsUnique();
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_prodattr_product");
            e.HasOne(x => x.Attribute).WithMany().HasForeignKey(x => x.AttributeId).HasConstraintName("fk_prodattr_attribute");
        });

        modelBuilder.Entity<Beneficiary>(e =>
        {
            e.ToTable("beneficiary");
            e.HasKey(x => x.BeneficiaryId);
            e.Property(x => x.BeneficiaryId).HasColumnName("beneficiary_id");
            e.Property(x => x.BeneficiaryName).HasColumnName("beneficiary_name").HasMaxLength(100).IsRequired();
            e.Property(x => x.BeneficiaryType).HasColumnName("beneficiary_type").HasMaxLength(20).IsRequired().HasDefaultValue("Other");
            e.Property(x => x.BeneficiaryEmailId).HasColumnName("beneficiary_email_id").HasMaxLength(80);
            e.Property(x => x.BeneficiaryContactNo).HasColumnName("beneficiary_contact_no").HasMaxLength(20);
            e.Property(x => x.BeneficiaryBankName).HasColumnName("beneficiary_bank_name").HasMaxLength(100);
            e.Property(x => x.BeneficiaryBankBranch).HasColumnName("beneficiary_bank_branch").HasMaxLength(100);
            e.Property(x => x.BeneficiaryIfsc).HasColumnName("beneficiary_ifsc").HasMaxLength(20);
            e.Property(x => x.BeneficiaryAccNo).HasColumnName("beneficiary_acc_no").HasMaxLength(30);
            e.Property(x => x.BeneficiaryAccType).HasColumnName("beneficiary_acc_type").HasMaxLength(20);
            e.Property(x => x.BeneficiaryPan).HasColumnName("beneficiary_pan").HasMaxLength(15);
        });

        modelBuilder.Entity<BeneficiaryAssignment>(e =>
        {
            e.ToTable("beneficiary_assignment");
            e.HasKey(x => x.AssignmentId);
            e.Property(x => x.AssignmentId).HasColumnName("assignment_id");
            e.Property(x => x.ProductId).HasColumnName("product_id").IsRequired();
            e.Property(x => x.BeneficiaryId).HasColumnName("beneficiary_id").IsRequired();
            e.HasIndex(x => new { x.ProductId, x.BeneficiaryId }).IsUnique();
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_benassign_product");
            e.HasOne(x => x.Beneficiary).WithMany().HasForeignKey(x => x.BeneficiaryId).HasConstraintName("fk_benassign_beneficiary");
        });

        modelBuilder.Entity<Cart>(e =>
        {
            e.ToTable("cart");
            e.HasKey(x => x.CartId);
            e.Property(x => x.CartId).HasColumnName("cart_id");
            e.Property(x => x.UserId).HasColumnName("user_id").IsRequired();
            e.Property(x => x.ProductId).HasColumnName("product_id").IsRequired();
            e.Property(x => x.Qty).HasColumnName("qty").IsRequired();
            e.Property(x => x.RentDays).HasColumnName("rent_days");
            e.HasIndex(x => new { x.UserId, x.ProductId }).IsUnique();
            e.HasOne(x => x.User).WithMany().HasForeignKey(x => x.UserId).HasConstraintName("fk_cart_user");
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_cart_product");
        });

        modelBuilder.Entity<MyShelf>(e =>
        {
            e.ToTable("my_shelf");
            e.HasKey(x => x.ShelfId);
            e.Property(x => x.ShelfId).HasColumnName("shelf_id");
            e.Property(x => x.UserId).HasColumnName("user_id").IsRequired();
            e.Property(x => x.ProductId).HasColumnName("product_id").IsRequired();
            e.HasIndex(x => new { x.UserId, x.ProductId }).IsUnique();
            e.HasOne(x => x.User).WithMany().HasForeignKey(x => x.UserId).HasConstraintName("fk_shelf_user");
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_shelf_product");
        });

        modelBuilder.Entity<LibraryPackage>(e =>
        {
            e.ToTable("library_package");
            e.HasKey(x => x.PackageId);
            e.Property(x => x.PackageId).HasColumnName("package_id");
            e.Property(x => x.Name).HasColumnName("name").HasMaxLength(50).IsRequired();
            e.HasIndex(x => x.Name).IsUnique();
            e.Property(x => x.Cost).HasColumnName("cost").HasPrecision(10, 2).IsRequired();
            e.Property(x => x.ValidityDays).HasColumnName("validity_days").IsRequired();
            e.Property(x => x.BookLimit).HasColumnName("book_limit").IsRequired();
            e.Property(x => x.Description).HasColumnName("description").HasMaxLength(255).IsRequired();
        });

        modelBuilder.Entity<Transaction>(e =>
        {
            e.ToTable("transactions");
            e.HasKey(x => x.TransactionId);
            e.Property(x => x.TransactionId).HasColumnName("transaction_id");
            e.Property(x => x.UserId).HasColumnName("user_id");
            e.Property(x => x.TotalAmount).HasColumnName("total_amount").HasPrecision(12, 2);
            e.Property(x => x.Status).HasColumnName("status").HasMaxLength(20).HasConversion<string>();
            e.Property(x => x.CreatedAt).HasColumnName("created_at");
            e.Property(x => x.TransactionType).HasColumnName("transaction_type").HasMaxLength(10).HasConversion<string>();
            e.HasOne(x => x.User).WithMany().HasForeignKey(x => x.UserId).HasConstraintName("fk_transaction_user");
        });

        modelBuilder.Entity<TransactionItem>(e =>
        {
            e.ToTable("transaction_items");
            e.HasKey(x => x.ItemId);
            e.Property(x => x.ItemId).HasColumnName("item_id");
            e.Property(x => x.TransactionId).HasColumnName("transaction_id");
            e.Property(x => x.ProductId).HasColumnName("product_id");
            e.Property(x => x.Price).HasColumnName("price").HasPrecision(10, 2);
            e.Property(x => x.Quantity).HasColumnName("quantity");
            e.HasOne(x => x.Transaction).WithMany(t => t.Items).HasForeignKey(x => x.TransactionId).HasConstraintName("fk_txitem_transaction");
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_txitem_product");
        });

        modelBuilder.Entity<RoyaltyCalculation>(e =>
        {
            e.ToTable("royalty_calculation");
            e.HasKey(x => x.RoycalId);
            e.Property(x => x.RoycalId).HasColumnName("roycal_id");
            e.Property(x => x.ItemId).HasColumnName("item_id").IsRequired();
            e.Property(x => x.RoycalTranDate).HasColumnName("roycal_trandate");
            e.Property(x => x.ProductId).HasColumnName("product_id").IsRequired();
            e.Property(x => x.TotalAmount).HasColumnName("total_amount").HasPrecision(12, 2);
            e.Property(x => x.RoyaltyPercent).HasColumnName("royalty_percent").HasPrecision(5, 2);
            e.Property(x => x.TotalRoyalty).HasColumnName("total_royalty").HasPrecision(12, 2);
            e.HasOne(x => x.TransactionItem).WithMany().HasForeignKey(x => x.ItemId).HasConstraintName("fk_roycal_item");
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_roycal_product");
        });

        modelBuilder.Entity<ProductBeneficiary>(e =>
        {
            e.ToTable("product_beneficiary");
            e.HasKey(x => x.ProdbenId);
            e.Property(x => x.ProdbenId).HasColumnName("prodben_id");
            e.Property(x => x.BeneficiaryId).HasColumnName("beneficiary_id").IsRequired();
            e.Property(x => x.ProductId).HasColumnName("product_id").IsRequired();
            e.Property(x => x.RoycalId).HasColumnName("roycal_id").IsRequired();
            e.Property(x => x.RoyaltyReceived).HasColumnName("royalty_received").HasPrecision(12, 2);
            e.HasOne(x => x.Beneficiary).WithMany().HasForeignKey(x => x.BeneficiaryId).HasConstraintName("fk_prodben_beneficiary");
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_prodben_product");
            e.HasOne(x => x.RoyaltyCalculation).WithMany().HasForeignKey(x => x.RoycalId).HasConstraintName("fk_prodben_roycal");
        });

        modelBuilder.Entity<LibraryPackagePurchase>(e =>
        {
            e.ToTable("library_package_purchase");
            e.HasKey(x => x.PurchaseId);
            e.Property(x => x.PurchaseId).HasColumnName("purchase_id");
            e.Property(x => x.TransactionId).HasColumnName("transaction_id").IsRequired();
            e.Property(x => x.UserId).HasColumnName("user_id").IsRequired();
            e.Property(x => x.PackageId).HasColumnName("package_id").IsRequired();
            e.Property(x => x.PackagePrice).HasColumnName("package_price").HasPrecision(12, 2).IsRequired();
            e.Property(x => x.AllowedBooks).HasColumnName("allowed_books").IsRequired();
            e.Property(x => x.AvgBookPrice).HasColumnName("avg_book_price").HasPrecision(12, 2).IsRequired();
            e.Property(x => x.PurchaseDate).HasColumnName("purchase_date").IsRequired();
            e.HasOne(x => x.Transaction).WithMany().HasForeignKey(x => x.TransactionId).HasConstraintName("fk_purchase_transaction");
            e.HasOne(x => x.User).WithMany().HasForeignKey(x => x.UserId).HasConstraintName("fk_purchase_user");
            e.HasOne(x => x.LibraryPackage).WithMany().HasForeignKey(x => x.PackageId).HasConstraintName("fk_purchase_package");
        });

        modelBuilder.Entity<LibraryPackagePurchaseItem>(e =>
        {
            e.ToTable("library_package_purchase_item");
            e.HasKey(x => x.ItemId);
            e.Property(x => x.ItemId).HasColumnName("item_id");
            e.Property(x => x.PurchaseId).HasColumnName("purchase_id").IsRequired();
            e.Property(x => x.ProductId).HasColumnName("product_id").IsRequired();
            e.Property(x => x.RoyaltyPercent).HasColumnName("royalty_percent").HasPrecision(5, 2).IsRequired();
            e.Property(x => x.RoyaltyAmount).HasColumnName("royalty_amount").HasPrecision(12, 2).IsRequired();
            e.HasOne(x => x.Purchase).WithMany(p => p.Items).HasForeignKey(x => x.PurchaseId).HasConstraintName("fk_purchitem_purchase");
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_purchitem_product");
        });

        modelBuilder.Entity<MyLibrary>(e =>
        {
            e.ToTable("my_library");
            e.HasKey(x => x.MyLibId);
            e.Property(x => x.MyLibId).HasColumnName("my_lib_id");
            e.Property(x => x.UserId).HasColumnName("user_id").IsRequired();
            e.Property(x => x.PackageId).HasColumnName("package_id");
            e.Property(x => x.ProductId).HasColumnName("product_id");
            e.Property(x => x.AccessType).HasColumnName("access_type").HasMaxLength(10).HasConversion<string>().IsRequired();
            e.Property(x => x.StartDate).HasColumnName("start_date");
            e.Property(x => x.EndDate).HasColumnName("end_date");
            e.Property(x => x.BooksAllowed).HasColumnName("books_allowed");
            e.Property(x => x.BooksTaken).HasColumnName("books_taken");
            e.HasOne(x => x.User).WithMany().HasForeignKey(x => x.UserId).HasConstraintName("fk_mylib_user");
            e.HasOne(x => x.LibraryPackage).WithMany().HasForeignKey(x => x.PackageId).HasConstraintName("fk_mylib_package");
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_mylib_product");
        });

        modelBuilder.Entity<ReadBook>(e =>
        {
            e.ToTable("pdf_book");
            e.HasKey(x => x.PdfId);
            e.Property(x => x.PdfId).HasColumnName("pdf_Id");
            e.Property(x => x.PdfData).HasColumnName("pdf_data").HasColumnType("LONGBLOB");
            e.Property(x => x.FileName).HasColumnName("file_name").HasMaxLength(255);
            e.Property(x => x.ProductId).HasColumnName("product_id").IsRequired();
            e.HasIndex(x => x.ProductId).IsUnique();
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_pdfbook_product");
        });

        modelBuilder.Entity<ProductCover>(e =>
        {
            e.ToTable("product_cover");
            e.HasKey(x => x.CoverId);
            e.Property(x => x.CoverId).HasColumnName("cover_id");
            e.Property(x => x.ImageData).HasColumnName("image_data").HasColumnType("LONGBLOB").IsRequired();
            e.Property(x => x.ContentType).HasColumnName("content_type").HasMaxLength(100).IsRequired();
            e.Property(x => x.ProductId).HasColumnName("product_id").IsRequired();
            e.HasIndex(x => x.ProductId).IsUnique();
            e.HasOne(x => x.Product).WithMany().HasForeignKey(x => x.ProductId).HasConstraintName("fk_cover_product");
        });
    }
}

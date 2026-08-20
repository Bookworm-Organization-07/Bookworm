using AutoMapper;
using BookwormApi.DTO;
using BookwormApi.Models;

namespace BookwormApi.AutoMapper;

public class MappingProfile : Profile
{
    public MappingProfile()
    {
        CreateMap<Genere, GenereDto>();
        CreateMap<Language, LanguageDto>();
        CreateMap<Author, AuthorDto>();
        CreateMap<Publisher, PublisherDto>();
        CreateMap<ProductType, ProductTypeDto>();

        CreateMap<Product, ProductDto>();
        CreateMap<Product, ProductResponseDto>()
            .ForMember(d => d.AuthorName, o => o.MapFrom(s => s.Author != null ? s.Author.Name : null));

        CreateMap<ProductRequestDto, Product>();

        // BeneficiaryController/LibraryPackageController go through GenericService,
        // which uses one DTO for both reading and writing (CreateAsync/UpdateAsync
        // map TDto straight to TEntity) - so each needs a map in both directions.
        CreateMap<Beneficiary, BeneficiaryDto>();
        CreateMap<BeneficiaryDto, Beneficiary>();

        CreateMap<LibraryPackage, LibraryPackageDto>();
        CreateMap<LibraryPackageDto, LibraryPackage>();

        CreateMap<Cart, CartDto>();
        CreateMap<MyShelf, MyShelfDto>();

        CreateMap<Transaction, TransactionDto>()
            .ForMember(d => d.Status, o => o.MapFrom(s => s.Status != null ? s.Status.ToString() : null))
            .ForMember(d => d.TransactionType, o => o.MapFrom(s => s.TransactionType != null ? s.TransactionType.ToString() : null));

        CreateMap<TransactionItem, TransactionItemDTO>()
            .ForMember(d => d.ProductName, o => o.MapFrom(s => s.Product != null ? s.Product.ProductName : null));

        CreateMap<User, AdminUserSummaryDto>();
    }
}

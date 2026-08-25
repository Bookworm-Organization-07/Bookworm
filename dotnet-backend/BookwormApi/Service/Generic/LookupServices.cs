using AutoMapper;
using BookwormApi.DTO;
using BookwormApi.Models;
using BookwormApi.Repository.Generic;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service.Generic;

public interface IProductTypeService : IGenericService<ProductTypeDto, int> { }
public class ProductTypeService : GenericService<ProductType, ProductTypeDto, int>, IProductTypeService
{
    public ProductTypeService(IGenericRepository<ProductType, int> repo, IMapper mapper) : base(repo, mapper) { }
}

public interface IGenereService : IGenericService<GenereDto, int> { }
public class GenereService : GenericService<Genere, GenereDto, int>, IGenereService
{
    public GenereService(IGenericRepository<Genere, int> repo, IMapper mapper) : base(repo, mapper) { }
}

public interface ILanguageService : IGenericService<LanguageDto, int> { }
public class LanguageService : GenericService<Language, LanguageDto, int>, ILanguageService
{
    public LanguageService(IGenericRepository<Language, int> repo, IMapper mapper) : base(repo, mapper) { }
}

public interface IBeneficiaryLookupService : IGenericService<BeneficiaryDto, int> { }
public class BeneficiaryLookupService : GenericService<Beneficiary, BeneficiaryDto, int>, IBeneficiaryLookupService
{
    public BeneficiaryLookupService(IGenericRepository<Beneficiary, int> repo, IMapper mapper) : base(repo, mapper) { }
}

public interface ILibraryPackageLookupService : IGenericService<LibraryPackageDto, int>
{
    Task<LibraryPackageDto> GetByNameAsync(string name);
}

public class LibraryPackageLookupService : GenericService<LibraryPackage, LibraryPackageDto, int>, ILibraryPackageLookupService
{
    private readonly IGenericRepository<LibraryPackage, int> _repo;
    private readonly IMapper _mapper;

    public LibraryPackageLookupService(IGenericRepository<LibraryPackage, int> repo, IMapper mapper) : base(repo, mapper)
    {
        _repo = repo;
        _mapper = mapper;
    }

    public async Task<LibraryPackageDto> GetByNameAsync(string name)
    {
        var entity = await _repo.Query().FirstOrDefaultAsync(p => p.Name == name)
                     ?? throw new ArgumentException($"Library package not found: {name}");
        return _mapper.Map<LibraryPackageDto>(entity);
    }
}

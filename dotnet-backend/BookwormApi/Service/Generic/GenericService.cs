using AutoMapper;
using BookwormApi.Repository.Generic;

namespace BookwormApi.Service.Generic;

public class GenericService<TEntity, TDto, TKey> : IGenericService<TDto, TKey> where TEntity : class, new()
{
    protected readonly IGenericRepository<TEntity, TKey> Repository;
    protected readonly IMapper Mapper;
    private readonly string _entityName;

    public GenericService(IGenericRepository<TEntity, TKey> repository, IMapper mapper)
    {
        Repository = repository;
        Mapper = mapper;
        _entityName = typeof(TEntity).Name;
    }

    public async Task<List<TDto>> GetAllAsync()
    {
        var entities = await Repository.GetAllAsync();
        return Mapper.Map<List<TDto>>(entities);
    }

    public async Task<TDto> GetByIdAsync(TKey id)
    {
        var entity = await Repository.GetByIdAsync(id) ?? throw NotFound(id);
        return Mapper.Map<TDto>(entity);
    }

    public async Task<TDto> CreateAsync(TDto dto)
    {
        var entity = Mapper.Map<TEntity>(dto);
        await Repository.AddAsync(entity);
        return Mapper.Map<TDto>(entity);
    }

    public async Task<TDto> UpdateAsync(TKey id, TDto dto)
    {
        var entity = await Repository.GetByIdAsync(id) ?? throw NotFound(id);
        Mapper.Map(dto, entity);
        Repository.Update(entity);
        await Repository.SaveChangesAsync();
        return Mapper.Map<TDto>(entity);
    }

    public async Task DeleteAsync(TKey id)
    {
        var entity = await Repository.GetByIdAsync(id) ?? throw NotFound(id);
        await Repository.DeleteAsync(entity);
    }

    private ArgumentException NotFound(TKey id) => new($"{_entityName} not found: {id}");
}

namespace BookwormApi.Service.Generic;

public interface IGenericService<TDto, TKey>
{
    Task<List<TDto>> GetAllAsync();
    Task<TDto> GetByIdAsync(TKey id);
    Task<TDto> CreateAsync(TDto dto);
    Task<TDto> UpdateAsync(TKey id, TDto dto);
    Task DeleteAsync(TKey id);
}

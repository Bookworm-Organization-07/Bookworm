namespace BookwormApi.Repository.Generic;

public interface IGenericRepository<TEntity, TKey> where TEntity : class
{
    IQueryable<TEntity> Query();
    Task<List<TEntity>> GetAllAsync();
    Task<TEntity?> GetByIdAsync(TKey id);
    Task<TEntity> AddAsync(TEntity entity);
    void Update(TEntity entity);
    Task DeleteAsync(TEntity entity);
    Task SaveChangesAsync();
}

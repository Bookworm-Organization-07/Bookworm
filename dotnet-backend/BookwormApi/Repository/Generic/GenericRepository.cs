using BookwormApi.Data;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Repository.Generic;

public class GenericRepository<TEntity, TKey> : IGenericRepository<TEntity, TKey> where TEntity : class
{
    protected readonly BookwormDbContext Context;
    protected readonly DbSet<TEntity> DbSet;

    public GenericRepository(BookwormDbContext context)
    {
        Context = context;
        DbSet = context.Set<TEntity>();
    }

    public IQueryable<TEntity> Query() => DbSet.AsQueryable();

    public Task<List<TEntity>> GetAllAsync() => DbSet.ToListAsync();

    public async Task<TEntity?> GetByIdAsync(TKey id)
    {
        if (id is null) return null;
        return await DbSet.FindAsync(id);
    }

    public async Task<TEntity> AddAsync(TEntity entity)
    {
        await DbSet.AddAsync(entity);
        await Context.SaveChangesAsync();
        return entity;
    }

    public void Update(TEntity entity)
    {
        DbSet.Update(entity);
    }

    public async Task DeleteAsync(TEntity entity)
    {
        DbSet.Remove(entity);
        await Context.SaveChangesAsync();
    }

    public Task SaveChangesAsync() => Context.SaveChangesAsync();
}

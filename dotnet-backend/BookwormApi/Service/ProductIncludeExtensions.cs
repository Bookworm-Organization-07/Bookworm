using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public static class ProductIncludeExtensions
{
    public static IQueryable<Product> IncludeProductDetails(this IQueryable<Product> query)
    {
        return query
            .Include(p => p.Genere)
            .Include(p => p.Language)
            .Include(p => p.Author)
            .Include(p => p.Publisher)
            .Include(p => p.ProductType);
    }
}

using AutoMapper;
using BookwormApi.Data;
using BookwormApi.DTO;
using BookwormApi.Models;
using Microsoft.EntityFrameworkCore;

namespace BookwormApi.Service;

public class CartService : ICartService
{
    private readonly BookwormDbContext _db;
    private readonly IMapper _mapper;

    public CartService(BookwormDbContext db, IMapper mapper)
    {
        _db = db;
        _mapper = mapper;
    }

    public async Task<List<CartDto>> GetCartAsync(User user)
    {
        var carts = await _db.Carts
            .Where(c => c.UserId == user.UserId)
            .Include(c => c.Product)
            .ToListAsync();
        await LoadProductDetails(carts.Select(c => c.Product));
        return _mapper.Map<List<CartDto>>(carts);
    }

    public async Task<CartDto> AddToCartAsync(User user, int productId, int? qty, int? rentDays)
    {
        var product = await _db.Products.FindAsync(productId)
                      ?? throw new ArgumentException($"Product not found: {productId}");

        var quantity = (qty is null || qty < 1) ? 1 : qty.Value;

        if (rentDays is not null)
        {
            if (rentDays < 1)
            {
                throw new ArgumentException("Rental length must be at least one day.");
            }
            if (!product.Rentable)
            {
                throw new InvalidOperationException($"{product.ProductName} is not available to rent.");
            }
        }

        var existing = await _db.Carts.FirstOrDefaultAsync(c => c.UserId == user.UserId && c.ProductId == productId);
        if (existing is not null)
        {
            existing.Qty += quantity;
            existing.RentDays = rentDays;
        }
        else
        {
            existing = new Cart
            {
                UserId = user.UserId,
                ProductId = productId,
                Qty = quantity,
                RentDays = rentDays
            };
            _db.Carts.Add(existing);
        }

        await _db.SaveChangesAsync();
        existing.Product = product;
        await LoadProductDetails(new[] { product });
        return _mapper.Map<CartDto>(existing);
    }

    public async Task<CartDto> UpdateQtyAsync(User user, int cartId, int? qty)
    {
        if (qty is null || qty < 1)
        {
            throw new ArgumentException("Quantity must be at least 1.");
        }

        var cart = await OwnedCartItemAsync(user, cartId);
        cart.Qty = qty.Value;
        await _db.SaveChangesAsync();

        var product = await _db.Products.IncludeProductDetails().FirstAsync(p => p.ProductId == cart.ProductId);
        cart.Product = product;
        return _mapper.Map<CartDto>(cart);
    }

    public async Task RemoveItemAsync(User user, int cartId)
    {
        var cart = await OwnedCartItemAsync(user, cartId);
        _db.Carts.Remove(cart);
        await _db.SaveChangesAsync();
    }

    public async Task ClearCartAsync(User user)
    {
        var carts = await _db.Carts.Where(c => c.UserId == user.UserId).ToListAsync();
        _db.Carts.RemoveRange(carts);
        await _db.SaveChangesAsync();
    }

    private async Task<Cart> OwnedCartItemAsync(User user, int cartId)
    {
        var cart = await _db.Carts.FirstOrDefaultAsync(c => c.CartId == cartId)
                   ?? throw new ArgumentException("Cart item not found.");
        if (cart.UserId != user.UserId)
        {
            throw new ArgumentException("Cart item not found.");
        }
        return cart;
    }

    private async Task LoadProductDetails(IEnumerable<Product> products)
    {
        var ids = products.Select(p => p.ProductId).Distinct().ToList();
        if (ids.Count == 0) return;
        var full = await _db.Products.IncludeProductDetails().Where(p => ids.Contains(p.ProductId)).ToDictionaryAsync(p => p.ProductId);
        foreach (var p in products)
        {
            if (full.TryGetValue(p.ProductId, out var detailed))
            {
                p.Genere = detailed.Genere;
                p.Language = detailed.Language;
                p.Author = detailed.Author;
                p.Publisher = detailed.Publisher;
                p.ProductType = detailed.ProductType;
            }
        }
    }
}

namespace BookwormApi.Service;

public interface IProductCoverService
{
    Task SaveCoverAsync(int productId, Stream fileStream, string contentType);
    Task<(byte[] Data, string ContentType)> GetCoverAsync(int productId);
}

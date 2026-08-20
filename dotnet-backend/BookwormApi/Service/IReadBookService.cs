namespace BookwormApi.Service;

public interface IReadBookService
{
    Task SavePdfAsync(int productId, Stream fileStream, string fileName);
    Task<(byte[] Data, string FileName)> ReadBookAsync(int productId);
}

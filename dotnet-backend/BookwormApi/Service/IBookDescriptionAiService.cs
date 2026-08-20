namespace BookwormApi.Service;

public interface IBookDescriptionAiService
{
    bool IsConfigured { get; }
    Task<string> DraftShortDescriptionAsync(string longDescription);
}

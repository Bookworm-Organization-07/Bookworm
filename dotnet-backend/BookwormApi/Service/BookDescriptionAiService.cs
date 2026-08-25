using Microsoft.Extensions.AI;

namespace BookwormApi.Service;

public class BookDescriptionAiService : IBookDescriptionAiService
{
    private readonly IChatClient? _chatClient;
    private readonly ILogger<BookDescriptionAiService> _logger;

    public BookDescriptionAiService(IServiceProvider serviceProvider, ILogger<BookDescriptionAiService> logger)
    {
        _chatClient = serviceProvider.GetService<IChatClient>();
        _logger = logger;
    }

    public bool IsConfigured => _chatClient is not null;

    public async Task<string> DraftShortDescriptionAsync(string longDescription)
    {
        if (_chatClient is null)
        {
            throw new InvalidOperationException("AI assistance is not configured.");
        }

        try
        {
            var messages = new List<ChatMessage>
            {
                new(ChatRole.System, "You write concise, appealing back-cover blurbs for books. Reply with the blurb only, under 40 words."),
                new(ChatRole.User, longDescription)
            };

            var response = await _chatClient.GetResponseAsync(messages);
            return response.Text?.Trim() ?? string.Empty;
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "AI short-description generation failed");
            throw new InvalidOperationException("AI assistance is not configured.");
        }
    }
}

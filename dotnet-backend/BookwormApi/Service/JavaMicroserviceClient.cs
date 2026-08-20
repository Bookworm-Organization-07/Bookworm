using System.Net.Http.Json;

namespace BookwormApi.Service;

public class JavaMicroserviceClient
{
    private readonly HttpClient _httpClient;
    private readonly ILogger<JavaMicroserviceClient> _logger;

    public JavaMicroserviceClient(HttpClient httpClient, ILogger<JavaMicroserviceClient> logger)
    {
        _httpClient = httpClient;
        _logger = logger;
    }

    public async Task<string> GetProductTypesAsync()
    {
        try
        {
            var response = await _httpClient.GetAsync("/api/product-types");
            response.EnsureSuccessStatusCode();
            return await response.Content.ReadAsStringAsync();
        }
        catch (Exception ex)
        {
            _logger.LogWarning(ex, "Could not reach the Java backend for the microservice demo call");
            throw new InvalidOperationException(
                "Could not reach the Java backend. This is a demo endpoint only - it is not required for the .NET backend's core functionality.");
        }
    }
}

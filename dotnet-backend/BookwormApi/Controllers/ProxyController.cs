using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/proxy")]
[AllowAnonymous]
public class ProxyController : ControllerBase
{
    private readonly JavaMicroserviceClient _javaClient;

    public ProxyController(JavaMicroserviceClient javaClient) => _javaClient = javaClient;

    [HttpGet("java-product-types")]
    public async Task<IActionResult> ForwardToJavaBackend()
    {
        var data = await _javaClient.GetProductTypesAsync();
        return Content(data, "application/json");
    }
}

using BookwormApi.Service.Generic;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/languages")]
[AllowAnonymous]
public class LanguageController : ControllerBase
{
    private readonly ILanguageService _service;

    public LanguageController(ILanguageService service) => _service = service;

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _service.GetAllAsync());
}

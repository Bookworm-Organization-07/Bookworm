using BookwormApi.Service.Generic;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/generes")]
[AllowAnonymous]
public class GenereController : ControllerBase
{
    private readonly IGenereService _service;

    public GenereController(IGenereService service) => _service = service;

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _service.GetAllAsync());
}

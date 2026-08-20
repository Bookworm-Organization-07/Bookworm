using BookwormApi.DTO;
using BookwormApi.Service.Generic;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/library-packages")]
public class LibraryPackageController : ControllerBase
{
    private readonly ILibraryPackageLookupService _service;

    public LibraryPackageController(ILibraryPackageLookupService service) => _service = service;

    [HttpGet]
    [AllowAnonymous]
    public async Task<IActionResult> GetAll() => Ok(await _service.GetAllAsync());

    [HttpGet("{id:int}")]
    [AllowAnonymous]
    public async Task<IActionResult> GetById(int id) => Ok(await _service.GetByIdAsync(id));

    [HttpGet("name/{name}")]
    [AllowAnonymous]
    public async Task<IActionResult> GetByName(string name) => Ok(await _service.GetByNameAsync(name));

    [HttpPost]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<IActionResult> Create([FromBody] LibraryPackageDto dto)
    {
        var created = await _service.CreateAsync(dto);
        return StatusCode(StatusCodes.Status201Created, created);
    }

    [HttpDelete("{id:int}")]
    [Authorize(Roles = "ROLE_ADMIN")]
    public async Task<IActionResult> Delete(int id)
    {
        await _service.DeleteAsync(id);
        return NoContent();
    }
}

using BookwormApi.Service.Generic;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/product-types")]
[AllowAnonymous]
public class ProductTypeController : ControllerBase
{
    private readonly IProductTypeService _service;

    public ProductTypeController(IProductTypeService service) => _service = service;

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _service.GetAllAsync());
}

using BookwormApi.Service;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace BookwormApi.Controllers;

[ApiController]
[Route("api/product-beneficiaries")]
[Authorize(Roles = "ROLE_ADMIN")]
public class ProductBeneficiaryController : ControllerBase
{
    private readonly IProductBeneficiaryService _service;

    public ProductBeneficiaryController(IProductBeneficiaryService service) => _service = service;

    [HttpGet]
    public async Task<IActionResult> GetAll() => Ok(await _service.GetAllAsync());
}

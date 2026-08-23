using System.Text;
using BookwormApi.AutoMapper;
using BookwormApi.Data;
using BookwormApi.Middleware;
using BookwormApi.Repository.Generic;
using BookwormApi.Security;
using BookwormApi.Service;
using BookwormApi.Service.Generic;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.AI;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi;
using OpenAI;
using QuestPDF.Infrastructure;
using Serilog;

Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Information()
    .WriteTo.Console()
    .WriteTo.File("logs/bookworm-api-.log", rollingInterval: Serilog.RollingInterval.Day)
    .CreateLogger();

try
{
    var builder = WebApplication.CreateBuilder(args);
    builder.Host.UseSerilog();

    QuestPDF.Settings.License = LicenseType.Community;

    // Environment-variable overrides for DB creds, mirroring the Java backend's
    // ${DB_USERNAME:bookworm_app} / ${DB_PASSWORD:...} property placeholders.
    var connectionString = builder.Configuration.GetConnectionString("Default")!;
    var dbUsername = Environment.GetEnvironmentVariable("DB_USERNAME");
    var dbPassword = Environment.GetEnvironmentVariable("DB_PASSWORD");
    if (!string.IsNullOrEmpty(dbUsername) || !string.IsNullOrEmpty(dbPassword))
    {
        var csBuilder = new MySqlConnector.MySqlConnectionStringBuilder(connectionString);
        if (!string.IsNullOrEmpty(dbUsername)) csBuilder.UserID = dbUsername;
        if (!string.IsNullOrEmpty(dbPassword)) csBuilder.Password = dbPassword;
        connectionString = csBuilder.ConnectionString;
    }

    builder.Services.AddDbContext<BookwormDbContext>(options =>
        options.UseMySql(connectionString, ServerVersion.AutoDetect(connectionString)));

    builder.Services.Configure<JwtOptions>(builder.Configuration.GetSection("Jwt"));
    builder.Services.Configure<GoogleOptions>(builder.Configuration.GetSection("Google"));

    builder.Services.AddHttpContextAccessor();
    builder.Services.AddAutoMapper(cfg => { }, typeof(MappingProfile));

    // Generic CRUD infrastructure (mandatory requirement #6/#7)
    builder.Services.AddScoped(typeof(IGenericRepository<,>), typeof(GenericRepository<,>));
    builder.Services.AddScoped<IProductTypeService, ProductTypeService>();
    builder.Services.AddScoped<IGenereService, GenereService>();
    builder.Services.AddScoped<ILanguageService, LanguageService>();
    builder.Services.AddScoped<IBeneficiaryLookupService, BeneficiaryLookupService>();
    builder.Services.AddScoped<ILibraryPackageLookupService, LibraryPackageLookupService>();

    builder.Services.AddScoped<IJwtService, JwtService>();
    builder.Services.AddScoped<ICurrentUserService, CurrentUserService>();
    builder.Services.AddScoped<IAuthService, AuthService>();

    builder.Services.AddScoped<IProductService, ProductService>();
    builder.Services.AddScoped<ICartService, CartService>();
    builder.Services.AddScoped<IShelfService, ShelfService>();
    builder.Services.AddScoped<IMyLibraryService, MyLibraryService>();
    builder.Services.AddScoped<ICheckoutService, CheckoutService>();
    builder.Services.AddScoped<ILibraryCheckoutService, LibraryCheckoutService>();
    builder.Services.AddScoped<IBeneficiaryAssignmentService, BeneficiaryAssignmentService>();
    builder.Services.AddScoped<IProductBeneficiaryService, ProductBeneficiaryService>();
    builder.Services.AddScoped<IRoyaltyLedgerService, RoyaltyLedgerService>();
    builder.Services.AddScoped<IAdminUserService, AdminUserService>();
    builder.Services.AddScoped<IAdminDashboardService, AdminDashboardService>();
    builder.Services.AddScoped<IOrderHistoryService, OrderHistoryService>();
    builder.Services.AddScoped<ITransactionService, TransactionService>();
    builder.Services.AddScoped<IInvoiceService, InvoiceService>();

    builder.Services.AddScoped<IProductCoverService, ProductCoverService>();
    builder.Services.AddScoped<IReadBookService, ReadBookService>();
    builder.Services.AddScoped<ITransactionPdfService, TransactionPdfService>();
    builder.Services.AddScoped<ILibraryInvoicePdfService, LibraryInvoicePdfService>();

    builder.Services.AddScoped<IProductRowImportService, ProductRowImportService>();
    builder.Services.AddScoped<IExcelProductImportService, ExcelProductImportService>();

    // AI helper (Microsoft.Extensions.AI) - optional, registered only when configured
    builder.Services.AddScoped<IBookDescriptionAiService, BookDescriptionAiService>();
    var aiEndpoint = builder.Configuration["Ai:Endpoint"];
    var aiApiKey = builder.Configuration["Ai:ApiKey"];
    var aiModel = builder.Configuration["Ai:Model"] ?? "gpt-4o-mini";
    if (!string.IsNullOrWhiteSpace(aiEndpoint) && !string.IsNullOrWhiteSpace(aiApiKey))
    {
        builder.Services.AddSingleton<IChatClient>(_ =>
        {
            var openAiClient = new OpenAIClient(
                new System.ClientModel.ApiKeyCredential(aiApiKey),
                new OpenAIClientOptions { Endpoint = new Uri(aiEndpoint) });
            return openAiClient.GetChatClient(aiModel).AsIChatClient();
        });
    }

    // Java microservice demo (mandatory requirement #9)
    var javaBackendBaseUrl = builder.Configuration["JavaBackend:BaseUrl"] ?? "http://localhost:8081";
    builder.Services.AddHttpClient<JavaMicroserviceClient>(client =>
    {
        client.BaseAddress = new Uri(javaBackendBaseUrl);
        client.Timeout = TimeSpan.FromSeconds(5);
    });

    // Notification microservice - a second, purpose-built service (not a demo): invoice email
    // delivery lives entirely in BookwormNotificationService, called over HttpClient. Down or
    // unreachable, checkout still succeeds - see EmailService's catch block.
    var notificationServiceBaseUrl = builder.Configuration["NotificationService:BaseUrl"] ?? "http://localhost:8090";
    builder.Services.AddHttpClient<IEmailService, EmailService>(client =>
    {
        client.BaseAddress = new Uri(notificationServiceBaseUrl);
        client.Timeout = TimeSpan.FromSeconds(10);
    });

    builder.Services.AddHttpClient("Google", client =>
    {
        client.Timeout = TimeSpan.FromSeconds(10);
    });

    // JWT auth - claim types kept literal ("sub"/"role") to match the Java-issued token shape
    System.IdentityModel.Tokens.Jwt.JwtSecurityTokenHandler.DefaultMapInboundClaims = false;
    var jwtSecret = builder.Configuration["Jwt:Secret"]!;

    builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
        .AddJwtBearer(options =>
        {
            options.TokenValidationParameters = new TokenValidationParameters
            {
                ValidateIssuer = false,
                ValidateAudience = false,
                ValidateLifetime = true,
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtSecret)),
                NameClaimType = "sub",
                RoleClaimType = "role"
            };
            options.Events = new JwtBearerEvents
            {
                OnChallenge = async context =>
                {
                    context.HandleResponse();
                    context.Response.StatusCode = StatusCodes.Status401Unauthorized;
                    context.Response.ContentType = "application/json";
                    await context.Response.WriteAsync("{\"message\":\"Unauthorized\"}");
                },
                // Access and refresh tokens are signed with the same secret
                // (see JwtService), so a refresh token would otherwise pass
                // this same signature/expiry check and work as a Bearer
                // token too - only "token_type": "access" is accepted here.
                // Refresh tokens are only ever accepted at POST /api/auth/refresh
                // (JwtService.ValidateRefreshToken checks the opposite way round).
                OnTokenValidated = context =>
                {
                    var tokenType = context.Principal?.FindFirst("token_type")?.Value;
                    if (tokenType != "access")
                    {
                        context.Fail("This token cannot be used to access the API.");
                    }
                    return Task.CompletedTask;
                }
            };
        });

    builder.Services.AddAuthorization();

    var corsOrigins = builder.Configuration.GetSection("Cors:AllowedOrigins").Get<string[]>() ?? Array.Empty<string>();
    builder.Services.AddCors(options =>
    {
        options.AddPolicy("BookwormCors", policy =>
        {
            policy.WithOrigins(corsOrigins)
                .AllowAnyHeader()
                .AllowAnyMethod()
                .AllowCredentials();
        });
    });

    builder.Services.AddControllers()
        .ConfigureApiBehaviorOptions(options =>
        {
            // Data Annotation failures (mandatory requirement #5) would otherwise come back as
            // ValidationProblemDetails ({"errors": {...}}) - reshaped here to {"message": "..."}
            // so it matches ExceptionHandlingMiddleware's shape everywhere else in this API,
            // since the frontend reads err.response.data.message (see RegisterPage.jsx/LoginPage.jsx).
            options.InvalidModelStateResponseFactory = context =>
            {
                var firstError = context.ModelState.Values
                    .SelectMany(v => v.Errors)
                    .Select(e => e.ErrorMessage)
                    .FirstOrDefault() ?? "Invalid request.";
                return new BadRequestObjectResult(new { message = firstError });
            };
        });
    builder.Services.AddEndpointsApiExplorer();

    // Kestrel's own request-body cap (default 30MB) and ASP.NET Core's separate multipart
    // form parser cap (default 128MB) both sit below the 300MB bulk-upload limit the Java
    // backend allows (spring.servlet.multipart.max-request-size=300MB) - both must be raised,
    // [RequestSizeLimit] on the action alone only covers the Kestrel-level cap.
    builder.Services.Configure<Microsoft.AspNetCore.Http.Features.FormOptions>(options =>
    {
        options.MultipartBodyLengthLimit = 300_000_000;
    });
    builder.WebHost.ConfigureKestrel(options =>
    {
        options.Limits.MaxRequestBodySize = 300_000_000;
    });
    builder.Services.AddSwaggerGen(options =>
    {
        options.SwaggerDoc("v1", new OpenApiInfo { Title = "Bookworm .NET API", Version = "v1" });
        var scheme = new OpenApiSecurityScheme
        {
            Name = "Authorization",
            Type = SecuritySchemeType.Http,
            Scheme = "Bearer",
            BearerFormat = "JWT",
            In = ParameterLocation.Header,
            Description = "Enter a JWT: Bearer {token}"
        };
        options.AddSecurityDefinition("Bearer", scheme);
    });

    var app = builder.Build();

    if (app.Environment.IsDevelopment())
    {
        app.UseSwagger();
        app.UseSwaggerUI();
    }

    app.UseMiddleware<ExceptionHandlingMiddleware>();
    app.UseCors("BookwormCors");
    app.UseAuthentication();
    app.UseAuthorization();
    app.MapControllers();

    Log.Information("Bookworm .NET backend starting on {Urls}", string.Join(", ", app.Urls));
    app.Run();
}
catch (Microsoft.Extensions.Hosting.HostAbortedException)
{
    // Thrown by EF Core's design-time host resolution (e.g. `dotnet ef migrations add`) - not a real failure.
}
catch (Exception ex)
{
    Log.Fatal(ex, "Bookworm .NET backend terminated unexpectedly");
}
finally
{
    Log.CloseAndFlush();
}

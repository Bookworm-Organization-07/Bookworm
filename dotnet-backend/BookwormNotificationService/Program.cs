using BookwormNotificationService.Models;
using BookwormNotificationService.Services;
using Serilog;

Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Information()
    .WriteTo.Console()
    .WriteTo.File("logs/bookworm-notification-service-.log", rollingInterval: RollingInterval.Day)
    .CreateLogger();

try
{
    var builder = WebApplication.CreateBuilder(args);
    builder.Host.UseSerilog();

    builder.Services.Configure<MailOptions>(builder.Configuration.GetSection("Mail"));
    builder.Services.AddScoped<IEmailSenderService, EmailSenderService>();

    builder.Services.AddControllers();
    builder.Services.AddEndpointsApiExplorer();
    builder.Services.AddSwaggerGen(options =>
    {
        options.SwaggerDoc("v1", new Microsoft.OpenApi.OpenApiInfo
        {
            Title = "Bookworm Notification Service",
            Version = "v1",
            Description = "Standalone microservice: generates no data of its own, just delivers invoice emails on behalf of the main Bookworm .NET API."
        });
    });

    var app = builder.Build();

    if (app.Environment.IsDevelopment())
    {
        app.UseSwagger();
        app.UseSwaggerUI();
    }

    app.MapControllers();

    Log.Information("Bookworm Notification Service starting on {Urls}", string.Join(", ", app.Urls));
    app.Run();
}
catch (Exception ex)
{
    Log.Fatal(ex, "Bookworm Notification Service terminated unexpectedly");
}
finally
{
    Log.CloseAndFlush();
}

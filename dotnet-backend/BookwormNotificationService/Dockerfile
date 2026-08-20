# ---- Build stage ----
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src
COPY BookwormNotificationService.csproj ./
RUN dotnet restore BookwormNotificationService.csproj
COPY . .
RUN dotnet publish BookwormNotificationService.csproj -c Release -o /app --no-restore

# ---- Run stage ----
FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /app
COPY --from=build /app .
ENV ASPNETCORE_URLS=http://+:8090
EXPOSE 8090
ENTRYPOINT ["dotnet", "BookwormNotificationService.dll"]

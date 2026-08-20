# ---- Build stage ----
FROM mcr.microsoft.com/dotnet/sdk:9.0 AS build
WORKDIR /src
COPY BookwormApi.csproj ./
RUN dotnet restore BookwormApi.csproj
COPY . .
RUN dotnet publish BookwormApi.csproj -c Release -o /app --no-restore

# ---- Run stage ----
FROM mcr.microsoft.com/dotnet/aspnet:9.0
WORKDIR /app
COPY --from=build /app .
ENV ASPNETCORE_URLS=http://+:8080
EXPOSE 8080
ENTRYPOINT ["dotnet", "BookwormApi.dll"]

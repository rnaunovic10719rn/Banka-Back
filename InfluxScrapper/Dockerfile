FROM mcr.microsoft.com/dotnet/aspnet:6.0 AS base
WORKDIR /app
EXPOSE 80
EXPOSE 443

FROM mcr.microsoft.com/dotnet/sdk:6.0 AS build
WORKDIR /src
COPY ["InfluxScrapper/InfluxScrapper.csproj", "InfluxScrapper/"]
RUN dotnet restore "InfluxScrapper/InfluxScrapper.csproj"
COPY . .
WORKDIR "/src/InfluxScrapper"
RUN dotnet build "InfluxScrapper.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "InfluxScrapper.csproj" -c Release -o /app/publish

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "InfluxScrapper.dll"]

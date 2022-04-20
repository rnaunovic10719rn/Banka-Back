using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Controllers;
using InfluxScrapper.Influx;
using InfluxScrapper.Models.Exchange;
using InfluxScrapper.Models.Forex;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.Tests;

public class ForexExchangeRateUnitTests
{

    private static ForexExchangeInfluxScrapperController GenerateController()
    {
        var influxManager = new InfluxManager(Constants.InfluxDBUrl, Constants.InfluxToken, Constants.InfluxOrg,
            Constants.InfluxBucket);
        
        var serviceProvider = new ServiceCollection()
            .AddLogging()
            .AddHttpClient()
            .AddSingleton(influxManager)
            .BuildServiceProvider();
        
        var factory = serviceProvider.GetService<ILoggerFactory>();
        var logger = factory!.CreateLogger<ForexExchangeInfluxScrapperController>();
        var httpFactory = serviceProvider.GetService<IHttpClientFactory>();

        return new ForexExchangeInfluxScrapperController(httpFactory!, logger, influxManager!);
    }

    [Theory]
    [InlineData("usd", "eur")]
    [InlineData("jpy", "gbp")]
    public async Task TestUpdateAndReadForexExchangeRates(string from, string to)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));


        var query = new ForexExchangeRateCacheQuery()
        {
            Currencies = new []
            { 
                new ForexBody() {SymbolFrom = from, SymbolTo = to},
                new ForexBody() {SymbolFrom = to, SymbolTo = from}
                
            }
        };

        await controller.UpdateWait(query, tokenSource.Token);

        var result = await controller.ReadCache(query, tokenSource.Token);

        Assert.NotEmpty(result);
    }
    
    [Theory]
    [InlineData("rsd", "zar")]
    [InlineData("rsd", "rub")]
    public async Task TestReadForexExchangeRatesEmpty(string from, string to)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));

        var query = new ForexExchangeRateCacheQuery()
        {
            Currencies = new []
            { 
                new ForexBody() {SymbolFrom = from, SymbolTo = to},
                new ForexBody() {SymbolFrom = to, SymbolTo = from}
                
            }
        };

        var result = await controller.ReadCache(query, tokenSource.Token);

        Assert.Empty(result);
    }
}
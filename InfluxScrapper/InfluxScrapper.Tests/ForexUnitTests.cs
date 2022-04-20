using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Controllers;
using InfluxScrapper.Influx;
using InfluxScrapper.Models.Forex;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.Tests;

public class ForexUnitTests
{

    private static ForexInfluxScrapperController GenerateController()
    {
        var influxManager = new InfluxManager(Constants.InfluxDBUrl, Constants.InfluxToken, Constants.InfluxOrg,
            Constants.InfluxBucket);
        
        var serviceProvider = new ServiceCollection()
            .AddLogging()
            .AddHttpClient()
            .AddSingleton(influxManager)
            .BuildServiceProvider();
        
        var factory = serviceProvider.GetService<ILoggerFactory>();
        var logger = factory!.CreateLogger<ForexInfluxScrapperController>();
        var httpFactory = serviceProvider.GetService<IHttpClientFactory>();

        return new ForexInfluxScrapperController(httpFactory!, logger, influxManager!);
    }

    [Theory]
    [InlineData("usd", "eur", ForexType.Daily)]
    [InlineData("jpy", "gbp", ForexType.Monthly)]
    [InlineData("cad", "rsd", ForexType.Weekly)]
    public async Task TestUpdateAndReadForexNotIntraday(string from, string to, ForexType type)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));


        var updateQuery = new ForexQuery()
        {
            SymbolFrom = from,
            SymbolTo = to,
            Type = type,
        };

        await controller.UpdateWait(updateQuery, tokenSource.Token);

        var cacheQuery = new ForexCacheQuery()
        {
            SymbolFrom = from,
            SymbolTo = to,
            Type = type,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(60)),
            TimeTo = DateTime.Now.AddDays(3)
        };

        var result = await controller.ReadCache(cacheQuery, tokenSource.Token);

        Assert.NotEmpty(result);

        Assert.All(result,
            futureResult => { Assert.InRange(futureResult.Time, cacheQuery.TimeFrom.Value, cacheQuery.TimeTo.Value); });
    }


    [Theory]
    [InlineData("usd", "eur", "1min")]
    [InlineData("jpy", "gbp", "5min")]
    [InlineData("cad", "rsd", "15min")]
    [InlineData("gbp", "usd", "30min")]
    [InlineData("eur", "rsd", "60min")]
    public async Task TestUpdateAndReadForexIntraday(string from, string to, string interval)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));


        var updateQuery = new ForexQuery()
        {
            SymbolFrom = from,
            SymbolTo = to,
            Type = ForexType.Intraday,
            Interval = interval,
        };
        
        await controller.UpdateWait(updateQuery, tokenSource.Token);
        
        var cacheQuery = new ForexCacheQuery()
        {
            SymbolFrom = from,
            SymbolTo = to,
            Type = ForexType.Intraday,
            Interval = interval,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(60)),
            TimeTo = DateTime.Now.AddDays(3)
        };
        
        var result = await controller.ReadCache(cacheQuery, tokenSource.Token);

        Assert.NotEmpty(result);

        Assert.All(result,
            futureResult =>
            {
                Assert.InRange(futureResult.Time, cacheQuery.TimeFrom.Value, cacheQuery.TimeTo.Value);
            });
    }
    
    
    [Theory]
    [InlineData("rsd", "eur", ForexType.Daily)]
    [InlineData("rsd", "gbp", ForexType.Monthly)]
    [InlineData("rsd", "rsd", ForexType.Weekly)]
    public async Task TestReadForexNotIntradayEmpty(string from, string to, ForexType type)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));
        
        var cacheQuery = new ForexCacheQuery()
        {
            SymbolFrom = from,
            SymbolTo = to,
            Type = type,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(60)),
            TimeTo = DateTime.Now.AddDays(3)
        };
        
        var result = await controller.ReadCache(cacheQuery, tokenSource.Token);

        Assert.Empty(result);
    }
    
    
    [Theory]
    [InlineData("rsd", "eur", "1min")]
    [InlineData("rsd", "gbp", "5min")]
    [InlineData("rsd", "cny", "15min")]
    [InlineData("rsd", "usd", "30min")]
    [InlineData("rsd", "rsd", "60min")]
    public async Task TestUpdateAndReadForexIntradayEmpty(string from, string to, string interval)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));
        
        var cacheQuery = new ForexCacheQuery()
        {
            SymbolFrom = from,
            SymbolTo = to,
            Type = ForexType.Intraday,
            Interval = interval,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(60)),
            TimeTo = DateTime.Now.AddDays(3)
        };
        
        var result = await controller.ReadCache(cacheQuery, tokenSource.Token);

        Assert.Empty(result);
    }
    
    /*[Theory]
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

        await controller.UpdateWaitForexExchangeRate(query, tokenSource.Token);

        var result = await controller.ReadForexExchangeRate(query, tokenSource.Token);

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

        var result = await controller.ReadForexExchangeRate(query, tokenSource.Token);

        Assert.Empty(result);
    }*/
}
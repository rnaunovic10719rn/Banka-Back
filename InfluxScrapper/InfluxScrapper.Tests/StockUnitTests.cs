using System.Runtime.Versioning;
using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Controllers;
using InfluxScrapper.Influx;
using InfluxScrapper.Models.Stock;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.Tests;

public class StockUnitTests
{
    private static StockInfluxScrapperController GenerateController()
    {
        var influxManager = new InfluxManager(Constants.InfluxDBUrl, Constants.InfluxToken, Constants.InfluxOrg,
            Constants.InfluxBucket);
        
        var serviceProvider = new ServiceCollection()
            .AddLogging()
            .AddHttpClient()
            .AddSingleton(influxManager)
            .BuildServiceProvider();
        
        var factory = serviceProvider.GetService<ILoggerFactory>();
        var logger = factory!.CreateLogger<StockInfluxScrapperController>();
        var httpFactory = serviceProvider.GetService<IHttpClientFactory>();

        return new StockInfluxScrapperController(httpFactory!, logger, influxManager!);
    }

    [Theory]
    [InlineData("msft", StockType.Daily, 1)]
    [InlineData("aapl", StockType.Monthly, 2)]
    [InlineData("ibm", StockType.Weekly, 3)]
    public async Task TestUpdateAndReadStockNotIntraday(string ticker, StockType type, int months)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));


        var updateQuery = new StockUpdateQuery()
        {
            Symbol = ticker,
            Type = type,
            Months = months
        };

        await controller.UpdateWait(updateQuery, tokenSource.Token);

        var cacheQuery = new StockCacheQuery()
        {
            Symbol = ticker,
            Type = type,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(months * 30)),
            TimeTo = DateTime.Now.AddDays(3)
        };

        var result = await controller.ReadCache(cacheQuery, tokenSource.Token);

        Assert.NotEmpty(result);

        Assert.All(result,
            futureResult => { Assert.InRange(futureResult.Time, cacheQuery.TimeFrom.Value, cacheQuery.TimeTo.Value); });
    }


    [Theory]
    [InlineData("msft",  "1min", 1)]
    [InlineData("aapl",  "5min", 2)]
    [InlineData("ibm", "15min", 3)]
    [InlineData("nky", "30min", 1)]
    [InlineData("hsi", "60min", 2)]
    public async Task TestUpdateAndReadStockIntraday(string ticker, string interval, int months)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));


        var updateQuery = new StockUpdateQuery()
        {
            Symbol = ticker,
            Type = StockType.Intraday,
            Months = months,
            Interval = interval
        };
        
        await controller.UpdateWait(updateQuery, tokenSource.Token);
        
        var cacheQuery = new StockCacheQuery()
        {
            Symbol = ticker,
            Type = StockType.Intraday,
            Interval = interval,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(months * 30)),
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
    [InlineData("amzn", StockType.Daily)]
    [InlineData("aa", StockType.Monthly)]
    [InlineData("aac", StockType.Weekly)]
    public async Task TestReadStockNotIntradayEmpty(string ticker, StockType type)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));
        
        var cacheQuery = new StockCacheQuery()
        {
            Symbol = ticker,
            Type = type,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(30)),
            TimeTo = DateTime.Now.AddDays(3)
        };
        
        var result = await controller.ReadCache(cacheQuery, tokenSource.Token);

        Assert.Empty(result);
    }
    
    
    [Theory]
    [InlineData("pct", "1min")]
    [InlineData("pcx", "5min")]
    [InlineData("pdd", "15min")]
    [InlineData("pdex",  "30min")]
    [InlineData("pdfs",  "60min")]
    public async Task TestUpdateAndReadStockIntradayEmpty(string ticker, string interval)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));
        
        var cacheQuery = new StockCacheQuery()
        {
            Symbol = ticker,
            Type = StockType.Intraday,
            Interval = interval,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(30)),
            TimeTo = DateTime.Now.AddDays(3)
        };
        
        var result = await controller.ReadCache(cacheQuery, tokenSource.Token);

        Assert.Empty(result);
    }
}
/*using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Controllers;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Models.Stock;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.Tests;

public class StockUnitTests
{

    private static AlphaVantageStockScrapperController GenerateController()
    {
        var serviceProvider = new ServiceCollection()
            .AddLogging()
            .AddHttpClient()
            .BuildServiceProvider();
        
        var factory = serviceProvider.GetService<ILoggerFactory>();
        var logger = factory.CreateLogger<AlphaVantageStockScrapperController>();

        var httpFactory = serviceProvider.GetService<IHttpClientFactory>();
        return new AlphaVantageStockScrapperController(httpFactory!, logger);
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

        controller.UpdateWaitStock(updateQuery, tokenSource.Token);

        var cacheQuery = new StockCacheQuery()
        {
            Symbol = ticker,
            Type = type,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(months * 30)),
            TimeTo = DateTime.Now
        };

        var result = await controller.ReadStock(cacheQuery, tokenSource.Token);

        Assert.NotEmpty(result);

        Assert.All(result,
            futureResult => { Assert.InRange(futureResult.Date, cacheQuery.TimeFrom.Value, cacheQuery.TimeTo.Value); });
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
        
        controller.UpdateWaitStock(updateQuery, tokenSource.Token);
        
        var cacheQuery = new StockCacheQuery()
        {
            Symbol = ticker,
            Type = StockType.Intraday,
            Interval = interval,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(months * 30)),
            TimeTo = DateTime.Now
        };
        
        var result = await controller.ReadStock(cacheQuery, tokenSource.Token);

        Assert.NotEmpty(result);

        Assert.All(result,
            futureResult =>
            {
                Assert.InRange(futureResult.Date, cacheQuery.TimeFrom.Value, cacheQuery.TimeTo.Value);
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
            Type = StockType.Intraday,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(30)),
            TimeTo = DateTime.Now
        };
        
        var result = await controller.ReadStock(cacheQuery, tokenSource.Token);

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
            TimeTo = DateTime.Now
        };
        
        var result = await controller.ReadStock(cacheQuery, tokenSource.Token);

        Assert.Empty(result);
    }
    
    [Theory]
    [InlineData("msft", null, null)]
    [InlineData("aapl", "ibm", null)]
    [InlineData("ppg", "pphp", "prds")]
    public async Task TestUpdateAndReadStockQuote(string? ticker1, string? ticker2, string? ticker3)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));

        var symbols = new List<string>();
        if(ticker1 is not null)
            symbols.Add(ticker1);
        
        if(ticker2 is not null)
            symbols.Add(ticker2);
        
        if(ticker3 is not null)
            symbols.Add(ticker3);
        
        var query = new StockQuoteCacheQuery() { Symbols =symbols.ToArray()};
       
        await controller.UpdateWaitStockQuote(query, tokenSource.Token);

        var result = await controller.ReadStockQuote(query, tokenSource.Token);

        Assert.NotEmpty(result);
    }
    
    [Theory]
    [InlineData("swbi", null, null)]
    [InlineData("swch", "swk", null)]
    [InlineData("swn", "swss", "swvl")]
    public async Task TestReadStockExchangeRatesEmpty(string? ticker1, string? ticker2, string? ticker3)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(3));

        var symbols = new List<string>();
        if(ticker1 is not null)
            symbols.Add(ticker1);
        
        if(ticker2 is not null)
            symbols.Add(ticker2);
        
        if(ticker3 is not null)
            symbols.Add(ticker3);
        
        var query = new StockQuoteCacheQuery() { Symbols =symbols.ToArray()};

        await controller.UpdateWaitStockQuote(query, tokenSource.Token);

        var result = await controller.ReadStockQuote(query, tokenSource.Token);

        Assert.Empty(result);
    }
}*/
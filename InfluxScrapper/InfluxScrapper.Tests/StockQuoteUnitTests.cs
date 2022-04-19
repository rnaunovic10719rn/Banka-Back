using System.Runtime.Versioning;
using InfluxScrapper.Models.Quote;
using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Controllers;
using InfluxScrapper.Influx;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.Tests;

public class StockQuoteUnitTests
{
    private static StockQuoteInfluxScrapperController GenerateController()
    {
        var influxManager = new InfluxManager(Constants.InfluxDBUrl, Constants.InfluxToken, Constants.InfluxOrg,
            Constants.InfluxBucket);
        
        var serviceProvider = new ServiceCollection()
            .AddLogging()
            .AddHttpClient()
            .AddSingleton(influxManager)
            .BuildServiceProvider();
        
        var factory = serviceProvider.GetService<ILoggerFactory>();
        var logger = factory!.CreateLogger<StockQuoteInfluxScrapperController>();
        var httpFactory = serviceProvider.GetService<IHttpClientFactory>();

        return new StockQuoteInfluxScrapperController(httpFactory!, logger, influxManager!);
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
       
        await controller.UpdateWait(query, tokenSource.Token);

        var result = await controller.ReadCache(query, tokenSource.Token);

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

        await controller.UpdateWait(query, tokenSource.Token);

        var result = await controller.ReadCache(query, tokenSource.Token);

        Assert.Empty(result);
    }
}
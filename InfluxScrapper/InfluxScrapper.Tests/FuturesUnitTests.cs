/*using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Controllers;
using InfluxScrapper.Models.Future;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.Tests;

public class FuturesUnitTests
{

    private static NasdaqFuturesScrapperController GenerateController()
    {
        var serviceProvider = new ServiceCollection()
            .AddLogging()
            .AddHttpClient()
            .BuildServiceProvider();
        
        var factory = serviceProvider.GetService<ILoggerFactory>();
        var logger = factory.CreateLogger<NasdaqFuturesScrapperController>();

        var httpFactory = serviceProvider.GetService<IHttpClientFactory>();
        return new NasdaqFuturesScrapperController(httpFactory!, logger);
    }
    
    [Fact]
    public async Task TestUpdateAndReadFutureData()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        
        await controller.UpdateWaitFutureData(tokenSource.Token);
        var result = await controller.ReadFutureData(tokenSource.Token);

        Assert.NotEmpty(result);
    }
    
    [Theory]
    [InlineData("FVSJ2022")]
    [InlineData("FCEU2020")]
    public async Task TestUpdateAndReadFuture(string symbol)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));


        var updateQuery = new FutureQuery()
        {
            Symbol = symbol
        };
        
        await controller.UpdateWaitFuture(updateQuery, tokenSource.Token);
        
        var cacheQuery = new FutureCacheQuery()
        {
            Symbol = symbol,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(60)),
            TimeTo = DateTime.Now
        };
        
        var result = await controller.ReadFuture(cacheQuery, tokenSource.Token);

        Assert.NotEmpty(result);

        Assert.All(result,
            futureResult =>
            {
                Assert.InRange(futureResult.Date, cacheQuery.TimeFrom.Value, cacheQuery.TimeTo.Value);
            });
    }
    
    
    [Theory]
    [InlineData("FRDXZ2022")]
    [InlineData("FMWOH2019")]
    public async Task TestReadFutureEmpty(string symbol)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));

        var cacheQuery = new FutureCacheQuery()
        {
            Symbol = symbol,
            TimeFrom = DateTime.Now.Subtract(TimeSpan.FromDays(60)),
            TimeTo = DateTime.Now
        };
        
        var result = await controller.ReadFuture(cacheQuery, tokenSource.Token);

        Assert.Empty(result);
    }
    
}*/
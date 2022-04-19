using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Controllers;
using InfluxScrapper.Influx;
using InfluxScrapper.Models.Future;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.Tests;

public class FuturesUnitTests
{

    private static FutureInfluxScrapperController GenerateController()
    {
        var influxManager = new InfluxManager(Constants.InfluxDBUrl, Constants.InfluxToken, Constants.InfluxOrg,
            Constants.InfluxBucket);
        
        var serviceProvider = new ServiceCollection()
            .AddLogging()
            .AddHttpClient()
            .AddSingleton(influxManager)
            .BuildServiceProvider();
        
        var factory = serviceProvider.GetService<ILoggerFactory>();
        var logger = factory!.CreateLogger<FutureInfluxScrapperController>();
        var httpFactory = serviceProvider.GetService<IHttpClientFactory>();

        return new FutureInfluxScrapperController(httpFactory!, logger, influxManager!);
    }

    [Theory]
    [InlineData("FVSJ2022")]
    [InlineData("FCEU2020")]
    public async Task TestUpdateAndReadFuture(string symbol)
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));


        var updateQuery = new FutureCacheQuery()
        {
            Symbol = symbol
        };
        
        await controller.UpdateWait(updateQuery, tokenSource.Token);
        
        var cacheQuery = new FutureCacheQuery()
        {
            Symbol = symbol,
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
            TimeTo = DateTime.Now.AddDays(3)
        };
        
        var result = await controller.ReadCache(cacheQuery, tokenSource.Token);

        Assert.Empty(result);
    }
    
}
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

public class FutureDataUnitTests
{

    private static FutureDataScrapperController GenerateController()
    {
        var serviceProvider = new ServiceCollection()
            .AddLogging()
            .AddHttpClient()
            .BuildServiceProvider();
        
        var factory = serviceProvider.GetService<ILoggerFactory>();
        var logger = factory!.CreateLogger<FutureDataScrapperController>();
        var httpFactory = serviceProvider.GetService<IHttpClientFactory>();

        return new FutureDataScrapperController(httpFactory!, logger);
    }

    
    [Fact]
    public async Task TestUpdateAndReadFutureData()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        
        await controller.UpdateWait(tokenSource.Token);
        var result = await controller.ReadCache(tokenSource.Token);

        Assert.NotEmpty(result);
    }
}
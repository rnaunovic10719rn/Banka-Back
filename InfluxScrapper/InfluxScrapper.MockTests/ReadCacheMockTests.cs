using System;
using System.Linq;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Models.Influx;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.MockTests;

public class ReadCacheMockTests
{
    private static (MockController, MockInfluxManager)  GenerateController()
    {
        var influxManager = new MockInfluxManager();
        
        var serviceProvider = new ServiceCollection()
            .AddLogging()
            .AddHttpClient()
            .AddSingleton(influxManager)
            .BuildServiceProvider();
        
        var factory = serviceProvider.GetService<ILoggerFactory>();
        var logger = factory!.CreateLogger<MockController>();
        var httpFactory = serviceProvider.GetService<IHttpClientFactory>();

        var controller = new MockController(httpFactory!, logger, influxManager!);
        controller.ScrapeWaitTimeout  = TimeSpan.FromSeconds(5);
        controller.ScrapeDelayTime = TimeSpan.FromSeconds(1);
        controller.ChacheValiditySpan = TimeSpan.FromSeconds(15);
        return (controller, influxManager);
    }

        
    [Fact]
    public async Task TestReadCacheEmpty()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.ReadCache(new MockReadQuery(2, 0), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
    
    [Fact]
    public async Task TestReadCache()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.ReadCache(new MockReadQuery(2, 1), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
    
    [Fact]
    public async Task TestReadCacheThrottle()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.ReadCache(new MockReadQuery(2, 2), tokenSource.Token);
        Assert.Empty(influx.Items);
    }

    [Fact]
    public async Task TestReadCacheTimeout()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.ReadCache(new MockReadQuery(2, 3), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
        
    [Fact]
    public async Task TestReadCacheErrorOnce()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(5));
        await controller.ReadCache(new MockReadQuery(2, 6), tokenSource.Token);
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.Empty(influx.Items);
    }
    
    [Fact]
    public async Task TestReadCacheSelfTimeout()
    {
        var (controller, influx) = GenerateController();
        await controller.ReadCache(new MockReadQuery(2, 6 * 15 + 1), CancellationToken.None);
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.Empty(influx.Items);
    }
}
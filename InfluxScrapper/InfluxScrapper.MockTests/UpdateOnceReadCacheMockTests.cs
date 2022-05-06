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

public class UpdateOnceReadCacheMockTests
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
    public async Task TestUpdateOnceReadCacheEmpty()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateOnceReadCache(new MockReadQuery(2, 0), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateOnceReadCache()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateOnceReadCache(new MockReadQuery(2, 1), tokenSource.Token);
        Assert.NotEmpty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateOnceReadCacheThrottle()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateOnceReadCache(new MockReadQuery(2, 2), tokenSource.Token);
        Assert.NotEmpty(influx.Items);
    }

    [Fact]
    public async Task TestUpdateOnceReadCacheTimeout()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateOnceReadCache(new MockReadQuery(2, 3), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
        
    [Fact]
    public async Task TestUpdateOnceReadCacheErrorOnce()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(5));
        await controller.UpdateOnceReadCache(new MockReadQuery(2, 6), tokenSource.Token);
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.NotEmpty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateOnceReadCacheSelfTimeout()
    {
        var (controller, influx) = GenerateController();
        await controller.UpdateOnceReadCache(new MockReadQuery(2, 6 * 15 + 1), CancellationToken.None);
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.Empty(influx.Items);
    }
        
    [Fact]
    public async Task TestUpdateOnceReadCacheCache()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(5));
        var updateQuery = new MockUpdateQuery(2, 1);
        var readQuery = new MockReadQuery(2, 1);
        await controller.UpdateWait(updateQuery, tokenSource.Token);
        var cacheItems = influx.Items.ToArray();
        await Task.Delay(TimeSpan.FromSeconds(2),tokenSource.Token);
        await controller.UpdateOnceReadCache(readQuery, tokenSource.Token);;
        Assert.NotEmpty(influx.Items);
        Assert.Equal(cacheItems.Length, influx.Items.Count);
        Assert.True(cacheItems.SequenceEqual(influx.Items));
    }
    
    [Fact]
    public async Task TestUpdateOnceReadCacheCacheExpired()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(20));
        var updateQuery = new MockUpdateQuery(2, 1);
        var readQuery = new MockReadQuery(2, 1);
        await controller.UpdateWait(updateQuery, tokenSource.Token);
        var cacheItems = influx.Items.ToArray();
        await Task.Delay(TimeSpan.FromSeconds(15),tokenSource.Token);
        await controller.UpdateOnceReadCache(readQuery, tokenSource.Token);;
        Assert.NotEmpty(influx.Items);
        Assert.False(cacheItems.SequenceEqual(influx.Items));
    }
}
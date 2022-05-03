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

public class UpdateWaitOnceMockTests
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
    public async Task TestUpdateWaitOnceEmpty()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWaitOnce(new MockUpdateQuery(2, 0), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateWaitOnce()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWaitOnce(new MockUpdateQuery(2, 1), tokenSource.Token);
        Assert.NotEmpty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateWaitOnceThrottle()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWaitOnce(new MockUpdateQuery(2, 2), tokenSource.Token);
        Assert.NotEmpty(influx.Items);
    }

    [Fact]
    public async Task TestUpdateWaitOnceTimeout()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWaitOnce(new MockUpdateQuery(2, 3), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
        
    [Fact]
    public async Task TestUpdateWaitOnceErrorOnce()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(5));
        await controller.UpdateWaitOnce(new MockUpdateQuery(2, 6), tokenSource.Token);
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.NotEmpty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateWaitOnceSelfTimeout()
    {
        var (controller, influx) = GenerateController();
        await controller.UpdateWaitOnce(new MockUpdateQuery(2, 6 * 15 + 1), CancellationToken.None);
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.Empty(influx.Items);
    }
        
    /*[Fact]
    public async Task TestUpdateWaitOnceCache()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(5));
        var updateQuery = new MockUpdateQuery(2, 1);
        await controller.UpdateWait(updateQuery, tokenSource.Token);
        var cacheItems = influx.Items.ToArray();
        await Task.Delay(TimeSpan.FromSeconds(2),tokenSource.Token);
        await controller.UpdateWaitOnce(updateQuery, tokenSource.Token);;
        Assert.NotEmpty(influx.Items);
        Assert.Equal(cacheItems.Length, influx.Items.Count);
        Assert.True(cacheItems.SequenceEqual(influx.Items));
    }
    
    [Fact]
    public async Task TestUpdateWaitOnceCacheExpired()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(20));
        var updateQuery = new MockUpdateQuery(2, 1);
        await controller.UpdateWait(updateQuery, tokenSource.Token);
        var cacheItems = influx.Items.ToArray();
        await Task.Delay(TimeSpan.FromSeconds(15),tokenSource.Token);
        await controller.UpdateWaitOnce(updateQuery, tokenSource.Token);;
        Assert.NotEmpty(influx.Items);
        Assert.False(cacheItems.SequenceEqual(influx.Items));
    }*/
}
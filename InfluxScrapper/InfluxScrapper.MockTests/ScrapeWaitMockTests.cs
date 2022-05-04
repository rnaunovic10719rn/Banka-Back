using System.Runtime.Versioning;
using System;
using System.Linq;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Controllers;
using InfluxScrapper.Influx;
using InfluxScrapper.Models.Controllers;
using InfluxScrapper.Models.Stock;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.MockTests;

public class ScrapeWaitMockTests
{
    private static MockController GenerateController()
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
        return controller;
    }

    [Fact]
    public async Task TestScrapeWaitEmpty()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        var results = await controller.ScrapeWait(new MockScrapeQuery(0), tokenSource.Token);
        Assert.Empty(results);
    }
    
    [Fact]
    public async Task TestScrapeWait()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        var results = await controller.ScrapeWait(new MockScrapeQuery(1), tokenSource.Token);
        Assert.NotEmpty(results);
        Assert.All(results, result =>
        {
            Assert.NotNull(result);
            Assert.InRange(result.Time, DateTime.Now.Subtract(TimeSpan.FromDays(366)), DateTime.Now);
            Assert.Equal(DateTimeKind.Utc, result.Time.Kind);
        });
    }
    
    [Fact]
    public async Task TestScrapeWaitThrottle()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        var results = await controller.ScrapeWait(new MockScrapeQuery(2), tokenSource.Token);
        Assert.NotEmpty(results);
    }
    
    [Fact]
    public async Task TestScrapeWaitTimeout()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        var results = await controller.ScrapeWait(new MockScrapeQuery(3), tokenSource.Token);
        Assert.Empty(results);
    }
    
    [Fact]
    public async Task TestScrapeWaitError()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        var results = await controller.ScrapeWait(new MockScrapeQuery(4), tokenSource.Token);
        Assert.Empty(results);
    }
        
    [Fact]
    public async Task TestScrapeWaitErrorOnce()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(5));
        var results = (await controller.ScrapeWait(new MockScrapeQuery(6), tokenSource.Token)).ToArray();
        Assert.NotEmpty(results);
        Assert.All(results, result =>
        {
            Assert.NotNull(result);
            Assert.InRange(result.Time, DateTime.Now.Subtract(TimeSpan.FromDays(366)), DateTime.Now);
            Assert.Equal(DateTimeKind.Utc, result.Time.Kind);
        });
    }
    
    [Fact]
    public async Task TestScrapeWaitSelfTimeout()
    {
        var controller = GenerateController();
        var results = await controller.ScrapeWait(new MockScrapeQuery(5 * 15 + 1), CancellationToken.None);
        Assert.Empty(results);
    }
}
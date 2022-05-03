using System.Runtime.Versioning;
using System;
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

public class StockMockTests
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

        return new MockController(httpFactory!, logger, influxManager!);
    }

    [Fact]
    public async Task TestScrapeEmpty()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        var results = await controller.Scrape(new MockScrapeQuery(0), tokenSource.Token);
        Assert.Empty(results);
    }
    
    [Fact]
    public async Task TestScrape()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        var results = await controller.Scrape(new MockScrapeQuery(1), tokenSource.Token);
        Assert.NotEmpty(results);
        Assert.All(results, result =>
        {
            Assert.NotNull(result);
            Assert.InRange(result.Time, DateTime.Now.Subtract(TimeSpan.FromDays(366)), DateTime.Now);
            Assert.Equal(DateTimeKind.Utc, result.Time.Kind);
        });
    }
    
    [Fact]
    public async Task TestScrapeThrottle()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        var results = await controller.Scrape(new MockScrapeQuery(2), tokenSource.Token);
        Assert.NotEmpty(results);
    }
    
    [Fact]
    public async Task TestScrapeTimeout()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        var results = await controller.Scrape(new MockScrapeQuery(3), tokenSource.Token);
        Assert.Empty(results);
    }
    
    [Fact]
    public async Task TestScrapeError()
    {
        var controller = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        var results = await controller.Scrape(new MockScrapeQuery(4), tokenSource.Token);
        Assert.Empty(results);
    }
}
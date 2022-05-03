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

public class StockUnitTests
{
    private static MockController GenerateController()
    {
        var influxManager = new InfluxManager(Constants.InfluxDBUrl, Constants.InfluxToken, Constants.InfluxOrg,
            Constants.InfluxBucket);
        
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
    public void TestScrape()
    {
        var controller = GenerateController();
        controller.Scrape(new MockScrapeQuery(0), CancellationToken.None);
    }
}
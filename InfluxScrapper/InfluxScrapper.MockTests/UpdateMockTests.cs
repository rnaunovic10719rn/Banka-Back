using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Models.Influx;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.MockTests;

public class UpdateMockTests
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
    public async Task TestUpdateEmpty()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(2, 0));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.Empty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdate()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(2, 1));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.NotEmpty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateThrottle()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(2, 2));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.NotEmpty(influx.Items);
    }

    [Fact]
    public async Task TestUpdateTimeout()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(2, 3));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.Empty(influx.Items);
    }

    [Fact]
    public async Task TestUpdateError()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(2, 4));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.Empty(influx.Items);
    }
        
    [Fact]
    public async Task TestUpdateErrorOnce()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(2, 6));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.NotEmpty(influx.Items);
    }
            
    [Fact]
    public async Task TestUpdateErrorMultiple()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(2, 5 * 5 + 1));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(5));
        Assert.Empty(influx.Items);
    }
}
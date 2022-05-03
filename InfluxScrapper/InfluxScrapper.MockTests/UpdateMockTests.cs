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

        return (new MockController(httpFactory!, logger, influxManager!), influxManager);
    }

        
    [Fact]
    public async Task TestUpdateEmpty()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(1, 0));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(2));
        Assert.Empty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdate()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(1, 1));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(2));
        Assert.NotEmpty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateThrottle()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(1, 2));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(2));
        Assert.NotEmpty(influx.Items);
    }

    [Fact]
    public async Task TestUpdateTimeout()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(1, 3));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(2));
        Assert.Empty(influx.Items);
    }

    [Fact]
    public async Task TestUpdateError()
    {
        var (controller, influx) = GenerateController();
        controller.Update(new MockUpdateQuery(1, 4));
        Assert.Empty(influx.Items);
        await Task.Delay(TimeSpan.FromSeconds(2));
        Assert.Empty(influx.Items);
    }
    
}
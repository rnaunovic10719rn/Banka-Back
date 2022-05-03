using System;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Models.Influx;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Xunit;

namespace InfluxScrapper.MockTests;

public class UpdateWaitMockTests
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
    public async Task TestUpdateWaitEmpty()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWait(new MockUpdateQuery(1, 0), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateWait()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWait(new MockUpdateQuery(1, 1), tokenSource.Token);
        Assert.NotEmpty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateWaitThrottle()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWait(new MockUpdateQuery(1, 2), tokenSource.Token);
        Assert.NotEmpty(influx.Items);
    }

    [Fact]
    public async Task TestUpdateWaitTimeout()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWait(new MockUpdateQuery(1, 3), tokenSource.Token);
        Assert.Empty(influx.Items);
    }

    [Fact]
    public async Task TestUpdateWaitError()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWait(new MockUpdateQuery(1, 4), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
    
}
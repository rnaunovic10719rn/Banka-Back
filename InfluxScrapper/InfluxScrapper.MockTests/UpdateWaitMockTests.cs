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

        var controller = new MockController(httpFactory!, logger, influxManager!);
        controller.ScrapeWaitTimeout  = TimeSpan.FromSeconds(5);
        controller.ScrapeDelayTime = TimeSpan.FromSeconds(1);
        controller.ChacheValiditySpan = TimeSpan.FromSeconds(15);
        return (controller, influxManager);
    }

        
    [Fact]
    public async Task TestUpdateWaitEmpty()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWait(new MockUpdateQuery(2, 0), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateWait()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWait(new MockUpdateQuery(2, 1), tokenSource.Token);
        Assert.NotEmpty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateWaitThrottle()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(5));
        await controller.UpdateWait(new MockUpdateQuery(2, 2), tokenSource.Token);
        Assert.NotEmpty(influx.Items);
    }

    [Fact]
    public async Task TestUpdateWaitTimeout()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWait(new MockUpdateQuery(2, 3), tokenSource.Token);
        Assert.Empty(influx.Items);
    }

    [Fact]
    public async Task TestUpdateWaitError()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(2));
        await controller.UpdateWait(new MockUpdateQuery(2, 4), tokenSource.Token);
        Assert.Empty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateWaitOneError()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(5));
        await controller.UpdateWait(new MockUpdateQuery(2, 6), tokenSource.Token);
        Assert.NotEmpty(influx.Items);
    }
    
    [Fact]
    public async Task TestUpdateWaitOnceSelfTimeout()
    {
        var (controller, influx) = GenerateController();
        var tokenSource = new CancellationTokenSource(TimeSpan.FromSeconds(5));
        await controller.UpdateWait(new MockUpdateQuery(2, 6 * 15 + 1), tokenSource.Token);
        Assert.Empty(influx.Items);
    }

}
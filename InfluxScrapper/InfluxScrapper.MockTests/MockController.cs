using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;
using InfluxScrapper.Influx;
using InfluxScrapper.Models.Controllers;
using Microsoft.Extensions.Logging;

namespace InfluxScrapper.MockTests;

public class MockController : InfluxScrapperController<MockUpdateQuery, MockScrapeQuery, MockReadQuery, MockResult>
{
    private static Random _random = new();
    
    public MockController(IHttpClientFactory httpClientFactory, ILogger<InfluxScrapperController<MockUpdateQuery, MockScrapeQuery, MockReadQuery, MockResult>> logger, InfluxManager influxManager) : base(httpClientFactory, logger, influxManager)
    {
    }
    public override IEnumerable<MockScrapeQuery> ConvertToScrapeQueriesInternal(MockUpdateQuery updateQuery) 
        => Enumerable.Range(0,updateQuery.Length)
            .Select(i => new MockScrapeQuery(i));

    public override MockUpdateQuery ConvertToUpdateQueryInternal(MockReadQuery readQuery, DateTime? lastFound) =>
        new(readQuery.Lenght);

    public override async Task<IEnumerable<MockResult>> ScrapeInternal(MockScrapeQuery scrapeQuery,
        CancellationToken token)
    {
        var lenght = _random.Next(100, 1000);
        var results = new MockResult[lenght];
        for (int i = 0; i < lenght; i++)
        {
            var result = new MockResult();
            result.Value = _random.Next();
            result.Time = DateTime.Now.Subtract(TimeSpan.FromDays(365)) + TimeSpan.FromDays(_random.Next(1, 364));
        }
        return results;
    }
}
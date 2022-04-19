using InfluxScrapper.Influx;
using InfluxScrapper.Models.Controllers;
using InfluxScrapper.Models.Future;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilites;
using Microsoft.AspNetCore.Mvc;
using NodaTime;
using NodaTime.Extensions;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("nasdaq/future")]
public class FutureInfluxScrapperController : InfluxScrapperController<FutureCacheQuery, FutureQuery, FutureCacheQuery,
    FutureResult>
{
    public FutureInfluxScrapperController(IHttpClientFactory httpClientFactory,
        ILogger<FutureInfluxScrapperController>
            logger, InfluxManager influxManager) : base(httpClientFactory, logger, influxManager)
    {
    }

    internal override IEnumerable<FutureQuery> ConvertToScrapeQueriesInternal(FutureCacheQuery updateQuery)
        => new []{ updateQuery };

    internal override FutureCacheQuery ConvertToUpdateQueryInternal(FutureCacheQuery readQuery, DateTime? lastFound)
        => readQuery;

    internal override async Task<IEnumerable<FutureResult>> ScrapeInternal(FutureQuery scrapeQuery,
        CancellationToken token)
    {
        var results = await HttpUtilities.GetCSV<FutureResult>(scrapeQuery.Url, _httpClientFactory, token);
        if (results == null)
            throw new NullReferenceException("HTTP failed");
        results.ForEach(r =>
        {
            r.Symbol = scrapeQuery.Symbol;
        });
        return results;
    }
}
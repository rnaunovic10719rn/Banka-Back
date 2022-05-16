using InfluxScrapper.Influx;
using InfluxScrapper.Models.Controllers;
using InfluxScrapper.Models.Forex;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilities;
using Microsoft.AspNetCore.Mvc;
using NodaTime;
using NodaTime.Extensions;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage/forex")]
public class ForexInfluxScrapperController : InfluxScrapperController<ForexQuery, ForexQuery, ForexCacheQuery,
    ForexResult>
{
    public ForexInfluxScrapperController(IHttpClientFactory httpClientFactory,
        ILogger<ForexInfluxScrapperController>
            logger, InfluxManager influxManager) : base(httpClientFactory, logger, influxManager)
    {
    }

    public override IEnumerable<ForexQuery> ConvertToScrapeQueriesInternal(ForexQuery updateQuery)
        => new []{ updateQuery };

    public override ForexQuery ConvertToUpdateQueryInternal(ForexCacheQuery readQuery, DateTime? lastFound)
        => readQuery;

    public override async Task<IEnumerable<ForexResult>> ScrapeInternal(ForexQuery scrapeQuery,
        CancellationToken token)
    {
        var results = await HttpUtilities.GetCSV<ForexResult>(scrapeQuery.Url, _httpClientFactory, token);
        if (results == null)
            throw new NullReferenceException("HTTP failed");
        results.ForEach(r =>
        {
            r.SymbolFrom = scrapeQuery.SymbolFrom;
            r.SymbolTo = scrapeQuery.SymbolTo;
        });
        return results;
    }
}
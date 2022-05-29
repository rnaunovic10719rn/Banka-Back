using InfluxScrapper.Influx;
using InfluxScrapper.Models.Controllers;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilities;
using Microsoft.AspNetCore.Mvc;
using NodaTime;
using NodaTime.Extensions;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage/stock")]
public class StockInfluxScrapperController : InfluxScrapperController<StockUpdateQuery, StockScrapeQuery, StockCacheQuery,
    StockResult>
{
    public StockInfluxScrapperController(IHttpClientFactory httpClientFactory,
        ILogger<StockInfluxScrapperController>
            logger, InfluxManager influxManager) : base(httpClientFactory, logger, influxManager)
    {
    }

    public override IEnumerable<StockScrapeQuery> ConvertToScrapeQueriesInternal(StockUpdateQuery updateQuery) 
        => updateQuery.ToScrapeQueries();

    public override StockUpdateQuery ConvertToUpdateQueryInternal(StockCacheQuery readQuery, DateTime? lastFound)
    {
        int? months = null;
        if (readQuery?.TimeFrom is not null || lastFound is not null)
        {
            var start = lastFound ?? readQuery?.TimeFrom;
            var period = Period.Between(start!.Value.ToLocalDateTime(), DateTime.Now.ToLocalDateTime());
            if (period.Years < 2)
            {
                months = period.Years * 12 + period.Months + (period.Days != 0 ? 1 : 0);
            }
        }
      
        return new StockUpdateQuery(readQuery!, months);
    }

    public override async Task<IEnumerable<StockResult>> ScrapeInternal(StockScrapeQuery scrapeQuery,
        CancellationToken token)
    {
        var results = await HttpUtilities.GetCSV<StockResult>(scrapeQuery.Url, _httpClientFactory, token);
        if (results == null)
            throw new NullReferenceException("HTTP failed");
        results.ForEach(r => r.Ticker = scrapeQuery.Symbol);
        return results;
    }
}
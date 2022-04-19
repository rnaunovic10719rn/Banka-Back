using InfluxScrapper.Influx;
using InfluxScrapper.Models.Controllers;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilites;
using Microsoft.AspNetCore.Mvc;
using NodaTime;
using NodaTime.Extensions;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage/stock")]
public class StockScrapperController : ScrapperController<StockUpdateQuery, StockScrapeQuery, StockCacheQuery,
    StockResult>
{
    public StockScrapperController(IHttpClientFactory httpClientFactory,
        ILogger<StockScrapperController>
            logger, InfluxManager influxManager) : base(httpClientFactory, logger, influxManager)
    {
    }

    internal override IEnumerable<StockScrapeQuery> ConvertToScrapeQueriesInternal(StockUpdateQuery updateQuery) 
        => updateQuery.ToScrapeQueries();

    internal override StockUpdateQuery ConvertToUpdateQueryInternal(StockCacheQuery readQuery)
    {
        int? months = null;
        if (readQuery?.TimeFrom is not null)
        {
            var period = Period.Between(readQuery.TimeFrom.Value.ToLocalDateTime(), DateTime.Now.ToLocalDateTime());
            if (period.Years < 2)
            {
                months = period.Years * 12 + period.Months + (period.Days != 0 ? 1 : 0);
            }
        }
      
        return new StockUpdateQuery(readQuery!, months);
    }

    internal override async Task<IEnumerable<StockResult>> ScrapeInternal(StockScrapeQuery scrapeQuery,
        CancellationToken token)
    {
        var results = await HttpUtilities.GetCSV<StockResult>(scrapeQuery.Url, _httpClientFactory, token);
        if (results == null)
            throw new NullReferenceException("HTTP failed");
        results.ForEach(r => r.Ticker = scrapeQuery.Symbol);
        return results;
    }
}
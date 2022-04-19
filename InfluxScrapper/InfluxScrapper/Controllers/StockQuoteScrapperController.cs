using InfluxScrapper.Influx;
using InfluxScrapper.Models.Controllers;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilites;
using Microsoft.AspNetCore.Mvc;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage/stock/quote2")]
public class StockQuoteScrapperController : ScrapperController<StockQuoteCacheQuery, StockQuoteQuery, StockQuoteCacheQuery,
    StockQuoteResult>
{
    public StockQuoteScrapperController(IHttpClientFactory httpClientFactory,
        ILogger<ScrapperController<StockQuoteCacheQuery, StockQuoteQuery, StockQuoteCacheQuery, StockQuoteResult>>
            logger, InfluxManager influxManager) : base(httpClientFactory, logger, influxManager)
    {
    }

    internal override IEnumerable<StockQuoteQuery> ConvertToScrapeQueriesInternal(StockQuoteCacheQuery updateQuery) 
        => updateQuery.ToQuotes();

    internal override StockQuoteCacheQuery ConvertToUpdateQueryInternal(StockQuoteCacheQuery readQuery) => readQuery;

    internal override async Task<IEnumerable<StockQuoteResult>> ScrapeInternal(StockQuoteQuery scrapeQuery,
        CancellationToken token)
    {
        var results = await HttpUtilities.GetCSV<StockQuoteResult>(scrapeQuery.Url, this._httpClientFactory, token);
        if (results == null)
            throw new NullReferenceException("HTTP failed");
        results.ForEach(r => r.Ticker = scrapeQuery.Symbol);
        return results;
    }
}
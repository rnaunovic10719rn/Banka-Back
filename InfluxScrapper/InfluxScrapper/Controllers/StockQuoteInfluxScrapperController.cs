using InfluxScrapper.Influx;
using InfluxScrapper.Models.Controllers;
using InfluxScrapper.Models.Quote;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilites;
using Microsoft.AspNetCore.Mvc;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage/stock/quote")]
public class StockQuoteInfluxScrapperController : InfluxScrapperController<StockQuoteCacheQuery, StockQuoteQuery, StockQuoteCacheQuery,
    StockQuoteResult>
{
    public StockQuoteInfluxScrapperController(IHttpClientFactory httpClientFactory,
        ILogger<StockQuoteInfluxScrapperController>
            logger, InfluxManager influxManager) : base(httpClientFactory, logger, influxManager)
    {
    }

    public override IEnumerable<StockQuoteQuery> ConvertToScrapeQueriesInternal(StockQuoteCacheQuery updateQuery) 
        => updateQuery.ToQuotes();

    public override StockQuoteCacheQuery ConvertToUpdateQueryInternal(StockQuoteCacheQuery readQuery, DateTime? lastFound) => readQuery;

    public override async Task<IEnumerable<StockQuoteResult>> ScrapeInternal(StockQuoteQuery scrapeQuery,
        CancellationToken token)
    {
        var results = await HttpUtilities.GetCSV<StockQuoteResult>(scrapeQuery.Url, this._httpClientFactory, token);
        if (results == null)
            throw new NullReferenceException("HTTP failed");
        results.ForEach(r => r.Ticker = scrapeQuery.Symbol);
        return results;
    }
}
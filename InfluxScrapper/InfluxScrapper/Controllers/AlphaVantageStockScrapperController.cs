using System.ComponentModel;
using System.Globalization;
using System.Text;
using CsvHelper;
using CsvHelper.Configuration;
using InfluxDB.Client;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilites;
using Microsoft.AspNetCore.Mvc;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage/stock")]
public class AlphaVantageStockScrapperController : Controller
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly ILogger<AlphaVantageStockScrapperController> _logger;

    public AlphaVantageStockScrapperController(IHttpClientFactory httpClientFactory,
        ILogger<AlphaVantageStockScrapperController> logger)
    {
        _httpClientFactory = httpClientFactory;
        _logger = logger;
    }

    /// <summary>
    /// Updates database cache without wait
    /// </summary>
    /// <param name="query"></param>
    [Description("Updates database cache without wait")]
    [HttpPost("update")]
    public void UpdateStock([FromBody] StockUpdateQuery query)
    {
        const int allowedScrapeMinutes = 60;
        var cancellationTokenSource = new CancellationTokenSource(TimeSpan.FromMinutes(allowedScrapeMinutes));
        foreach (var task in GenerateUpdateStockTasks(query, cancellationTokenSource.Token))
            Task.Run(async () => await task, cancellationTokenSource.Token).ConfigureAwait(false);
    }

    /// <summary>
    /// Updates database cache and waits for completion
    /// </summary>
    /// <param name="query"></param>
    /// <param name="token"></param>
    [Description("Updates database cache and waits for completion")]
    [HttpPost("updatewait")]
    public void UpdateWaitStock([FromBody] StockUpdateQuery query, CancellationToken token)
    {
        Task.WaitAll(GenerateUpdateStockTasks(query, token).ToArray(), token);
    }

    private IEnumerable<Task> GenerateUpdateStockTasks(StockUpdateQuery query, CancellationToken token)
        => query.ToScrapeQueries().Select(scrapeQuery => UpdateStock(scrapeQuery, token));

    private async Task<bool> UpdateStock(StockScrapeQuery query, CancellationToken token)
    {
        try
        {
            var measurement = query.Measurement;
            var results = await ScrapeStock(query, token);
            if (results is null)
                return false;
            await InfluxDBUtilites.UploadData(results, r => r.ToPointData(measurement), token);
            _logger.LogInformation("Writing done");
            return true;
        }
        catch (Exception ex)
        {
            _logger.LogError(new EventId(0), ex, "Update exception");
            return false;
        }
    }


    [Description("Gets data directly from scrapping website")]
    [HttpPost("scrape")]
    public Task<IEnumerable<StockResult>?> ScrapeStock([FromBody] StockScrapeQuery query, CancellationToken token) =>
        RetryUtilities.Scrape(_logger,
            () => AlphaVantageScrapper.ScrapeStock(query, _httpClientFactory),
            token);

    [Description("Gets data directly from scrapping website")]
    [HttpPost("scrapewait")]
    public Task<IEnumerable<StockResult>?> ScrapeStockWait([FromBody] StockScrapeQuery query, CancellationToken token) =>
        RetryUtilities.ScrapeRetry(_logger,
            () => AlphaVantageScrapper.ScrapeStock(query, _httpClientFactory),
            token);

    [Description("Gets cached data")]
    [HttpPost("read")]
    public async Task<IEnumerable<StockResult>> ReadStock([FromBody] StockCacheQuery query, CancellationToken token)
    {
        var queryStr = InfluxDBUtilites.ConstructQuery(query);
        try
        {
            var result = await InfluxDBUtilites.ParseQuery(queryStr, StockResult.FromRecord, token);
            return result;
        }
        catch
        {
            return Enumerable.Empty<StockResult>();
        }
    }


    [Description("Updates database cache without wait")]
    [HttpPost("quote/update")]
    public void UpdateStockQuote([FromBody] StockQuoteCacheQuery query)
        => RetryUtilities.ScheduleUpdates(_logger, GenerateUpdateStockQuoteTasks(query).ToArray());

    [Description("Updates database cache and waits for completion")]
    [HttpPost("quote/updatewait")]
    public async Task<bool> UpdateWaitStockQuote([FromBody] StockQuoteCacheQuery query, CancellationToken token) =>
        (await RetryUtilities.UpdateWaitAll(_logger, token, GenerateUpdateStockQuoteTasks(query).ToArray()))
        .All(r => r);

    [Description("Updates database cache and waits once for completion")]
    [HttpPost("quote/updatewaitonce")]
    public async Task<bool> UpdateWaitStockOnce([FromBody] StockQuoteCacheQuery query, CancellationToken token) =>
        (await RetryUtilities.UpdateWaitOnceOrScheduleUpdate(_logger, token,
            GenerateUpdateStockQuoteTasks(query).ToArray()))
        .All(r => r);

    private IEnumerable<Func<CancellationToken, Task<bool>>> GenerateUpdateStockQuoteTasks(StockQuoteCacheQuery query)
        => query.ToQuotes().Select(scrapeQuery => new Func<CancellationToken, Task<bool>>(
            token => RetryUtilities.Update(_logger
                , AlphaVantageScrapper.ScrapeStockQuote(scrapeQuery, _httpClientFactory),
                r => r.ToPointData(query.Measurement), token)));


    [Description("Gets data directly from scrapping website")]
    [HttpPost("quote/scrape")]
    public Task<IEnumerable<StockQuoteResult>?> ScrapeStockQuote([FromBody] StockQuoteQuery query,
        CancellationToken token) =>
        RetryUtilities.Scrape(_logger,
            () => AlphaVantageScrapper.ScrapeStockQuote(query, _httpClientFactory),
            token);

    [Description("Gets data directly from scrapping website and repeats untill sucssess or timeout")]
    [HttpPost("quote/scrapewait")]
    public Task<IEnumerable<StockQuoteResult>?> ScrapeStockQuoteWait([FromBody] StockQuoteQuery query,
        CancellationToken token) =>
        RetryUtilities.ScrapeRetry(_logger,
            () => AlphaVantageScrapper.ScrapeStockQuote(query, _httpClientFactory),
            token);


    [Description("Reads cached data")]
    [HttpPost("quote/read")]
    public Task<IEnumerable<StockQuoteResult>> ReadStockQuote([FromBody] StockQuoteCacheQuery cacheQuery,
        CancellationToken token) =>
        RetryUtilities.Query(_logger, InfluxDBUtilites.ConstructQuery(cacheQuery, true), StockQuoteResult.FromRecord,
            token);
    
    [Description("Updates data, if fails schedule updating and reads cached data")]
    [HttpPost("quote/updateread")]
    public async Task<IEnumerable<StockQuoteResult>> UpdateOnceReadStockQuote([FromBody] StockQuoteCacheQuery cacheQuery,
        CancellationToken token)
    {
        await UpdateWaitStockOnce(cacheQuery, token);
        return await RetryUtilities.Query(_logger, InfluxDBUtilites.ConstructQuery(cacheQuery, true),
            StockQuoteResult.FromRecord,
            token);
    }
}
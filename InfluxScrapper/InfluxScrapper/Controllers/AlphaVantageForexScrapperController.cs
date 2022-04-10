using System.ComponentModel;
using System.Globalization;
using System.Text;
using CsvHelper;
using CsvHelper.Configuration;
using InfluxDB.Client;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Forex;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilites;
using Microsoft.AspNetCore.Mvc;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage/forex")]
public class AlphaVantageForexScrapperController : Controller
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly ILogger<AlphaVantageForexScrapperController> _logger;

    public AlphaVantageForexScrapperController(IHttpClientFactory httpClientFactory, ILogger<AlphaVantageForexScrapperController> logger)
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
    public void UpdateForex([FromBody] ForexQuery query)
    {
        const int allowedScrapeMinutes = 60;
        var cancellationTokenSource = new CancellationTokenSource(allowedScrapeMinutes * 60000);
        Task.Run(async () => await UpdateForex(query, cancellationTokenSource.Token), cancellationTokenSource.Token).ConfigureAwait(false);
    }

    /// <summary>
    /// Updates database cache and waits for completion
    /// </summary>
    /// <param name="query"></param>
    /// <param name="token"></param>
    [Description("Updates database cache and waits for completion")]    
    [HttpPost("updatewait")]
    public async Task UpdateWaitForex([FromBody] ForexQuery query, CancellationToken token)
    {
        await UpdateForex(query, token);
    }


    private async Task UpdateForex(ForexQuery query, CancellationToken token)
    {
        try
        {
            var measurement = query.Measurement;
            var results = await ScrapeForex(query, token);
            var points = new List<PointData>();
            using var client = InfluxDBClientFactory.Create(Constants.InfluxDBUrl, Constants.InfluxToken);
            var writeApi = client.GetWriteApiAsync();
            foreach (var result in results)
            {
                result.SymbolFrom = query.SymbolFrom;
                result.SymbolTo = query.SymbolTo;
                points.Add(result.ToPointData(measurement));
            }
            await writeApi.WritePointsAsync(points, Constants.InfluxBucket, Constants.InfluxOrg, token);
            _logger.LogInformation("Writing done");
        }
        catch(Exception ex)
        {
            _logger.LogError(new EventId(0), ex,"Update exception");
        }
        
    }

    /// <summary>
    /// Gets data directly from scrapping website
    /// </summary>
    /// <param name="query"></param>
    /// <param name="token"></param>
    /// <returns></returns>
    [Description("Gets data directly from scrapping website")]
    [HttpPost("scrape")]
    public async Task<IEnumerable<ForexResult>> ScrapeForex([FromBody] ForexQuery query, CancellationToken token)
    {
        while (true)
        {
            try
            {
                var httpClient = _httpClientFactory.CreateClient();
                var httpRequest = new HttpRequestMessage(HttpMethod.Get, query.Url);
                var httpResponseMessage = await httpClient.SendAsync(httpRequest);
                if (!httpResponseMessage.IsSuccessStatusCode)
                    return Enumerable.Empty<ForexResult>();

                await using var stream = await httpResponseMessage.Content.ReadAsStreamAsync();
                var reader = new StreamReader(stream);
                using var csv = new CsvReader(reader,
                    new CsvConfiguration(CultureInfo.InvariantCulture)
                    {
                        PrepareHeaderForMatch = args => args.Header.ToLower()
                    });
                var result = csv.GetRecords<ForexResult>();
                return result?.ToArray() ?? Enumerable.Empty<ForexResult>();
            }
            catch(Exception ex)
            {
                _logger.LogError(new EventId(1), ex,"Scrape Error");
            }
            
            _logger.LogInformation("Retrying");
            
            const int sleepMinutes = 1;
            await Task.Delay(sleepMinutes * 60000, token);
        }
    }

    /// <summary>
    /// Gets cached data
    /// </summary>
    /// <param name="query"></param>
    /// <param name="token"></param>
    /// <returns></returns>
    [Description("Gets cached data")]
    [HttpPost("read")]
    public async Task<IEnumerable<ForexResult>> ReadForex([FromBody] ForexCacheQuery query, CancellationToken token)
    {
        using var client = InfluxDBClientFactory.Create(Constants.InfluxDBUrl, Constants.InfluxToken);
        var queryApi = client.GetQueryApi();
        var builder = new StringBuilder();
        builder.AppendLine("import \"influxdata/influxdb/schema\"");
        builder.AppendLine($"from(bucket:\"{Constants.InfluxBucket}\")");
        if (query.TimeFrom is not null && query.TimeTo is not null)
            builder.AppendLine($"|> range(start: {DateTime.SpecifyKind(query.TimeFrom.Value, DateTimeKind.Utc):o}, " +
                               $"stop: {DateTime.SpecifyKind(query.TimeTo.Value, DateTimeKind.Utc):o})");
        else  if (query.TimeFrom is not null)
            builder.AppendLine($"|> range(start: {DateTime.SpecifyKind(query.TimeFrom.Value, DateTimeKind.Utc):o})");
        else
            builder.AppendLine("|> range(start: 0)");
        builder.AppendLine($"|> filter(fn: (r) => r[\"_measurement\"] == \"{query.Measurement}\" " +
                           $"and r[\"from\"] == \"{query.SymbolFrom}\" and r[\"to\"] == \"{query.SymbolTo}\")");
        builder.AppendLine("|> schema.fieldsAsCols()");
        var queryStr = builder.ToString();
        List<FluxTable> tables;
        try
        {
            tables = await queryApi.QueryAsync(queryStr, Constants.InfluxOrg, token);
        }
        catch
        {
            return Enumerable.Empty<ForexResult>();
        }
        return tables.SelectMany(table =>
            table.Records.Select(record => ForexResult.FromRecord(record)));
    }

    
    [Description("Updates database cache without wait")]
    [HttpPost("exchangerate/update")]
    public void UpdateForexExchangeRate([FromBody] ForexExchangeRateCacheQuery cacheCacheQuery)
        => RetryUtilities.ScheduleUpdates(_logger, GenerateUpdateForexExchangeRateTasks(cacheCacheQuery).ToArray());

    [Description("Updates database cache and waits for completion")]
    [HttpPost("exchangerate/updatewait")]
    public async Task<bool> UpdateWaitForexExchangeRate([FromBody] ForexExchangeRateCacheQuery cacheCacheQuery, CancellationToken token) =>
        (await RetryUtilities.UpdateWaitAll(_logger, token, GenerateUpdateForexExchangeRateTasks(cacheCacheQuery).ToArray()))
        .All(r => r);

    [Description("Updates database cache and waits once for completion")]
    [HttpPost("exchangerate/updatewaitonce")]
    public async Task<bool> UpdateWaitForexExchangeRateOnce([FromBody] ForexExchangeRateCacheQuery cacheCacheQuery, CancellationToken token) =>
        (await RetryUtilities.UpdateWaitOnceOrScheduleUpdate(_logger, token,
            GenerateUpdateForexExchangeRateTasks(cacheCacheQuery).ToArray()))
        .All(r => r);

    private IEnumerable<Func<CancellationToken, Task<bool>>> GenerateUpdateForexExchangeRateTasks(ForexExchangeRateCacheQuery cacheCacheQuery)
        => cacheCacheQuery.ToQuotes().Select(scrapeQuery => new Func<CancellationToken, Task<bool>>(
            token => RetryUtilities.Update(_logger
                , AlphaVantageScrapper.ScrapeForexExchangeRate(scrapeQuery, _httpClientFactory),
                r => r.ToPointData(cacheCacheQuery.Measurement), token)));


    [Description("Gets data directly from scrapping website")]
    [HttpPost("exchangerate/scrape")]
    public Task<IEnumerable<ForexExchangeRateResult>?> ScrapeForexExchangeRate([FromBody] ForexExchangeRateQuery cacheQuery,
        CancellationToken token) =>
        RetryUtilities.Scrape(_logger,
            () => AlphaVantageScrapper.ScrapeForexExchangeRate(cacheQuery, _httpClientFactory),
            token);

    [Description("Gets data directly from scrapping website and repeats untill sucssess or timeout")]
    [HttpPost("exchangerate/scrapewait")]
    public Task<IEnumerable<ForexExchangeRateResult>?> ScrapeForexExchangeRateForexExchangeRate([FromBody] ForexExchangeRateQuery cacheQuery,
        CancellationToken token) =>
        RetryUtilities.ScrapeRetry(_logger,
            () => AlphaVantageScrapper.ScrapeForexExchangeRate(cacheQuery, _httpClientFactory),
            token);


    [Description("Reads cached data")]
    [HttpPost("exchangerate/read")]
    public Task<IEnumerable<ForexExchangeRateResult>> ReadSForexExchangeRate([FromBody] ForexExchangeRateCacheQuery query,
        CancellationToken token) =>
        RetryUtilities.Query(_logger, InfluxDBUtilites.ConstructQuery(query, true), ForexExchangeRateResult.FromRecord,
            token);
    
    [Description("Updates data, if fails schedule updating and reads cached data")]
    [HttpPost("exchangerate/updateread")]
    public async Task<IEnumerable<ForexExchangeRateResult>> UpdateOnceReadForexExchangeRate([FromBody] ForexExchangeRateCacheQuery query,
        CancellationToken token)
    {
        await UpdateWaitForexExchangeRateOnce(query, token);
        return await RetryUtilities.Query(_logger, InfluxDBUtilites.ConstructQuery(query, true),
            ForexExchangeRateResult.FromRecord,
            token);
    }
    
}
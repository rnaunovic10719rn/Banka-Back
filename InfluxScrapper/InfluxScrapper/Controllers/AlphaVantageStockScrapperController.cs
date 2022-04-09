using System.ComponentModel;
using System.Globalization;
using CsvHelper;
using CsvHelper.Configuration;
using InfluxDB.Client;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Stock;
using Microsoft.AspNetCore.Mvc;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage/stock")]
public class AlphaVantageStockScrapperController : Controller
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly ILogger<AlphaVantageStockScrapperController> _logger;

    public AlphaVantageStockScrapperController(IHttpClientFactory httpClientFactory, ILogger<AlphaVantageStockScrapperController> logger)
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
        var cancellationTokenSource = new CancellationTokenSource(allowedScrapeMinutes * 60000);
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

    private async Task UpdateStock(StockScrapeQuery query, CancellationToken token)
    {
        try
        {
            var measurement = query.Measurement;
            var results = await ScrapeStock(query, token);
            var points = new List<PointData>();
            using var client = InfluxDBClientFactory.Create(Constants.InfluxDBUrl, Constants.InfluxToken);
            var writeApi = client.GetWriteApiAsync();
            foreach (var result in results)
            {
                result.Ticker = query.Symbol;
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
    public async Task<IEnumerable<StockResult>> ScrapeStock([FromBody] StockScrapeQuery query, CancellationToken token)
    {
        while (true)
        {
            try
            {
                var httpClient = _httpClientFactory.CreateClient();
                var httpRequest = new HttpRequestMessage(HttpMethod.Get, query.Url);
                var httpResponseMessage = await httpClient.SendAsync(httpRequest);
                if (!httpResponseMessage.IsSuccessStatusCode)
                    return Enumerable.Empty<StockResult>();

                await using var stream = await httpResponseMessage.Content.ReadAsStreamAsync();
                var reader = new StreamReader(stream);
                using var csv = new CsvReader(reader,
                    new CsvConfiguration(CultureInfo.InvariantCulture)
                    {
                        PrepareHeaderForMatch = args => args.Header.ToLower()
                    });
                var result = csv.GetRecords<StockResult>();
                return result?.ToArray() ?? Enumerable.Empty<StockResult>();
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
    public async Task<IEnumerable<StockResult>> ReadStock([FromBody] StockScrapeQuery query, CancellationToken token)
    {
        using var client = InfluxDBClientFactory.Create(Constants.InfluxDBUrl, Constants.InfluxToken);
        var queryApi = client.GetQueryApi();
        var influxQuery =
            "import \"influxdata/influxdb/schema\" " +
            $"from(bucket:\"{Constants.InfluxBucket}\") " +
            "|> range(start: 0) " +
            $"|> filter(fn: (r) => r[\"_measurement\"] == \"{query.Measurement}\") "
            +"|> schema.fieldsAsCols() ";
        var tables = await queryApi.QueryAsync(influxQuery, Constants.InfluxOrg, token);
        return tables.SelectMany(table =>
            table.Records.Select(record => StockResult.FromRecord(record)));
    }

}
using System.ComponentModel;
using System.Globalization;
using CsvHelper;
using CsvHelper.Configuration;
using InfluxDB.Client;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Forex;
using InfluxScrapper.Models.Stock;
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
        Task.Run(async () => await UpdateForex(query, cancellationTokenSource.Token)).ConfigureAwait(false);
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
    public async Task<IEnumerable<ForexResult>> ReadForex([FromBody] ForexQuery query, CancellationToken token)
    {
        using var client = InfluxDBClientFactory.Create(Constants.InfluxDBUrl, Constants.InfluxToken);
        var queryApi = client.GetQueryApi();
        var influxQuery =
            "import \"influxdata/influxdb/schema\" " +
            "from(bucket:\"Forexs\") " +
            "|> range(start: 0) " +
            $"|> filter(fn: (r) => r[\"_measurement\"] == \"{query.Measurement}\") "
            +"|> schema.fieldsAsCols() ";
        var tables = await queryApi.QueryAsync(influxQuery, Constants.InfluxOrg, token);
        return tables.SelectMany(table =>
            table.Records.Select(record => ForexResult.FromRecord(record)));
    }

}
using System.Globalization;
using System.Net;
using System.Net.Http.Headers;
using System.Text;
using System.Text.Json;
using CsvHelper;
using CsvHelper.Configuration;
using InfluxDB.Client;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Server.HttpSys;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage")]
public class AlphaVantageScrapperController : Controller
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly ILogger<AlphaVantageScrapperController> _logger;

    public AlphaVantageScrapperController(IHttpClientFactory httpClientFactory, ILogger<AlphaVantageScrapperController> logger)
    {
        _httpClientFactory = httpClientFactory;
        _logger = logger;
    }

    [HttpPost("stock/update")]
    public void UpdateStock([FromBody] StockUpdateQuery query)
    {
        foreach (var task in GenerateUpdateStockTasks(query))
            Task.Run(async () => await task).ConfigureAwait(false);
    }

    [HttpPost("stock/updatewait")]
    public void UpdateWaitStock([FromBody] StockUpdateQuery query, CancellationToken token)
    {
        Task.WaitAll(GenerateUpdateStockTasks(query).ToArray(), token);
    }

    private IEnumerable<Task> GenerateUpdateStockTasks(StockUpdateQuery query) 
        => query.ToScrapeQueries().Select(scrapeQuery => UpdateStock(query, scrapeQuery));

    private async Task UpdateStock(StockUpdateQuery updateQuery, StockScrapeQuery query)
    {
        try
        {
            var measurement = $"stock_{updateQuery.Type.ToString().ToLower()}_{updateQuery.Symbol.ToLower()}";
            var results = await ScrapeStock(query);
            var points = new List<PointData>();
            using var client = InfluxDBClientFactory.Create(Constants.InfluxDBUrl, Constants.InfluxToken);
            var writeApi = client.GetWriteApiAsync();
            foreach (var result in results)
            {
                result.Ticker = query.Symbol;
                points.Add(result.ToPointData(measurement));
            }
            await writeApi.WritePointsAsync(points, Constants.InfluxBucket, Constants.InfluxOrg, CancellationToken.None);
            _logger.LogInformation("Writing done");
        }
        catch(Exception ex)
        {
            _logger.LogError(new EventId(0), ex,"Update exception");
        }
        
    }


    [HttpPost("scrape")]
    public async Task<IEnumerable<Stock>> ScrapeStock([FromBody] StockScrapeQuery query)
    {
        var httpClient = _httpClientFactory.CreateClient();
        var httpRequest = new HttpRequestMessage(HttpMethod.Get, query.Url);
        var httpResponseMessage = await httpClient.SendAsync(httpRequest);
        if (!httpResponseMessage.IsSuccessStatusCode)
            return Enumerable.Empty<Stock>();

        await using var stream = await httpResponseMessage.Content.ReadAsStreamAsync();
        var reader = new StreamReader(stream);
        using var csv = new CsvReader(reader,
            new CsvConfiguration(CultureInfo.InvariantCulture)
            {
                PrepareHeaderForMatch = args => args.Header.ToLower()
            });
        var result = csv.GetRecords<Stock>();
        return result?.ToArray() ?? Enumerable.Empty<Stock>();
    }
}
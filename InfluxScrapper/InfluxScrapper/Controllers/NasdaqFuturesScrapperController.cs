using System.ComponentModel;
using System.Globalization;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using CsvHelper;
using CsvHelper.Configuration;
using InfluxDB.Client;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Future;
using InfluxScrapper.Models.Future;
using InfluxScrapper.Models.Stock;
using Microsoft.AspNetCore.Mvc;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("nasdaq/futures")]
public class NasdaqFuturesScrapperController : Controller
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly ILogger<NasdaqFuturesScrapperController> _logger;

    public NasdaqFuturesScrapperController(IHttpClientFactory httpClientFactory, ILogger<NasdaqFuturesScrapperController> logger)
    {
        _httpClientFactory = httpClientFactory;
        _logger = logger;
    }


    /// <summary>
    /// Updates database cache without wait
    /// </summary>
    /// <param name="query"></param>
    [Description("Updates database cache without wait")]    
    [HttpPost("data/update")]
    public void UpdateFutureData()
    {
        const int allowedScrapeMinutes = 10;
        var cancellationTokenSource = new CancellationTokenSource(allowedScrapeMinutes * 60000);
        Task.Run(async () => await UpdateWaitFutureData( cancellationTokenSource.Token), cancellationTokenSource.Token).ConfigureAwait(false);;
    }
    
    /// <summary>
    /// Updates database cache and waits for completion
    /// </summary>
    /// <param name="query"></param>
    /// <param name="token"></param>
    [Description("Updates database cache and waits for completion")]
    [HttpPost("data/updatewait")]
    public async Task UpdateWaitFutureData(CancellationToken token)
    {

        var results = await ScrapeFutureData(token);
        try
        {
            string json = JsonSerializer.Serialize(results);
            if (!System.IO.Directory.Exists("storage"))
                System.IO.Directory.CreateDirectory("storage");
            if(System.IO.File.Exists("storage/futuredata.json"))
                System.IO.File.Delete("storage/futuredata.json");
            await System.IO.File.WriteAllTextAsync(@"storage/futuredata.json", json);
            _logger.LogInformation("Writing done");
        }
        catch (Exception ex)
        {
            _logger.LogError(new EventId(0), ex, "Update exception");
        }
    }

    /// <summary>
    /// Gets data directly from scrapping website
    /// </summary>
    /// <param name="token"></param>
    /// <returns></returns>
    [Description("Gets data directly from scrapping website")]
    [HttpPost("data/scrape")]
    public async Task<IEnumerable<FutureDataResult>> ScrapeFutureData(CancellationToken token)
    {
        while (true)
        {
            try
            {
                var httpClient = _httpClientFactory.CreateClient();
                var httpRequest = new HttpRequestMessage(HttpMethod.Get, "https://static.quandl.com/Ticker+CSV%27s/Futures/EUREX.csv");
                var httpResponseMessage = await httpClient.SendAsync(httpRequest);
                if (!httpResponseMessage.IsSuccessStatusCode)
                    return Enumerable.Empty<FutureDataResult>();

                await using var stream = await httpResponseMessage.Content.ReadAsStreamAsync();
                var reader = new StreamReader(stream);
                using var csv = new CsvReader(reader,
                    new CsvConfiguration(CultureInfo.InvariantCulture)
                    {
                        PrepareHeaderForMatch = args => args.Header.ToLower()
                    });
                var result = csv.GetRecords<FutureDataResult>();
                return result?.ToArray() ?? Enumerable.Empty<FutureDataResult>();
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
    /// <param name="token"></param>
    /// <returns></returns>
    [Description("Gets cached data")]
    [HttpPost("data/read")]
    public async Task<IEnumerable<FutureDataResult>> ReadFutureData(CancellationToken token)
    {
        try
        {
            if(!System.IO.File.Exists(@"storage/futuredata.json"))
                return Enumerable.Empty<FutureDataResult>();
            await using var stream = System.IO.File.OpenRead(@"storage/futuredata.json");
            return await JsonSerializer.DeserializeAsync<IEnumerable<FutureDataResult>> (stream, cancellationToken: token) ?? Enumerable.Empty<FutureDataResult>();
        }
        catch(Exception ex)
        {
            _logger.LogError(new EventId(1), ex,"Scrape Error");
        }
        return Enumerable.Empty<FutureDataResult>();
    }
    
    
    
    /// <summary>
    /// Updates database cache without wait
    /// </summary>
    /// <param name="query"></param>
    [Description("Updates database cache without wait")]    
    [HttpPost("update")]
    public void UpdateFuture([FromBody]FutureQuery query)
    {
        const int allowedScrapeMinutes = 10;
        var cancellationTokenSource = new CancellationTokenSource(allowedScrapeMinutes * 60000);
        Task.Run(async () => await UpdateWaitFuture(query, cancellationTokenSource.Token), cancellationTokenSource.Token).ConfigureAwait(false);;
    }
    

    /// <summary>
    /// Updates database cache and waits for completion
    /// </summary>
    /// <param name="query"></param>
    /// <param name="token"></param>
    [Description("Updates database cache and waits for completion")]
    [HttpPost("updatewait")]
    public async Task UpdateWaitFuture([FromBody]FutureQuery query, CancellationToken token)
    {

        var results = await ScrapeFuture(query, token);
        var points = new List<PointData>();
        using var client = InfluxDBClientFactory.Create(Constants.InfluxDBUrl, Constants.InfluxToken);
        var measurement = "futures";

        try
        {
            var writeApi = client.GetWriteApiAsync();
            foreach (var result in results)
            {
                result.Symbol = query.Symbol;
                points.Add(result.ToPointData(measurement));
            }

            await writeApi.WritePointsAsync(points, Constants.InfluxBucket, Constants.InfluxOrg, token);
            _logger.LogInformation("Writing done");
        }
        catch (Exception ex)
        {
            _logger.LogError(new EventId(0), ex, "Update exception");
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
    public async Task<IEnumerable<FutureResult>> ScrapeFuture([FromBody] FutureQuery query, CancellationToken token)
    {
        while (true)
        {
            try
            {
                var httpClient = _httpClientFactory.CreateClient();
                var httpRequest = new HttpRequestMessage(HttpMethod.Get, query.Url);
                var httpResponseMessage = await httpClient.SendAsync(httpRequest);
                if (!httpResponseMessage.IsSuccessStatusCode)
                    return Enumerable.Empty<FutureResult>();

                await using var stream = await httpResponseMessage.Content.ReadAsStreamAsync();
                var reader = new StreamReader(stream);
                using var csv = new CsvReader(reader,
                    new CsvConfiguration(CultureInfo.InvariantCulture)
                    {
                        PrepareHeaderForMatch = args => args.Header.ToLower()
                    });
                var result = csv.GetRecords<FutureResult>();
                return result?.ToArray() ?? Enumerable.Empty<FutureResult>();
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
    /// <param name="token"></param>
    /// <returns></returns>
    [Description("Gets cached data")]
    [HttpPost("read")]
    public async Task<IEnumerable<FutureResult>> ReadFuture([FromBody] FutureCacheQuery query, CancellationToken token)
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
        builder.AppendLine($"|> filter(fn: (r) => r[\"_measurement\"] == \"futures\" and" +
                           $" r[\"symbol\"] == \"{query.Symbol}\" )");
        builder.AppendLine("|> schema.fieldsAsCols()");
        var queryStr = builder.ToString();
        List<FluxTable> tables;
        try
        {
            tables = await queryApi.QueryAsync(queryStr, Constants.InfluxOrg, token);
        }
        catch
        {
            return Enumerable.Empty<FutureResult>();
        }
        
        return tables.SelectMany(table =>
            table.Records.Select(record => FutureResult.FromRecord(record)));
    }

}
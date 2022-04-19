using System.ComponentModel;
using System.Globalization;
using System.Text.Json;
using CsvHelper;
using CsvHelper.Configuration;
using InfluxScrapper.Future;
using Microsoft.AspNetCore.Mvc;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("nasdaq/futures/data")]
public class FutureDataScrapperController : Controller
{
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly ILogger<FutureDataScrapperController> _logger;

    public FutureDataScrapperController(IHttpClientFactory httpClientFactory, ILogger<FutureDataScrapperController> logger)
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
    
}
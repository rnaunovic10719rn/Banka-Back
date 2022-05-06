using System.ComponentModel;
using InfluxScrapper.Influx;
using InfluxScrapper.Models.Influx;
using Microsoft.AspNetCore.Mvc;

namespace InfluxScrapper.Models.Controllers;

public abstract class InfluxScrapperController<TUpdateQuery, TScrapeQuery, TReadQuery, TResult>
    where TResult : InvfluxRecord<TResult> where TReadQuery : InfluxCacheQuery<TReadQuery> 
    where TUpdateQuery : IInfluxMeasurementHolder
{
    internal readonly IHttpClientFactory _httpClientFactory;
    private readonly ILogger<InfluxScrapperController<TUpdateQuery, TScrapeQuery, TReadQuery, TResult>> _logger;
    private readonly IInfluxManager _influxManager;
    public TimeSpan ScrapeWaitTimeout { get; set; } = TimeSpan.FromMinutes(5);
    public TimeSpan ScrapeDelayTime { get; set; } = TimeSpan.FromMinutes(1);
    public TimeSpan ChacheValiditySpan { get; set; } = TimeSpan.FromMinutes(15);

    public InfluxScrapperController(IHttpClientFactory httpClientFactory,
        ILogger<InfluxScrapperController<TUpdateQuery, TScrapeQuery, TReadQuery, TResult>> logger,
        IInfluxManager influxManager)
    {
        _httpClientFactory = httpClientFactory;
        _logger = logger;
        _influxManager = influxManager;
    }


    [ApiExplorerSettings(IgnoreApi = true)]
    public abstract IEnumerable<TScrapeQuery> ConvertToScrapeQueriesInternal(TUpdateQuery updateQuery);
    [ApiExplorerSettings(IgnoreApi = true)]
    public abstract TUpdateQuery ConvertToUpdateQueryInternal(TReadQuery readQuery, DateTime? lastFound);
    [ApiExplorerSettings(IgnoreApi = true)]
    public abstract Task<IEnumerable<TResult>> ScrapeInternal(TScrapeQuery scrapeQuery, CancellationToken token);

    private string ControllerName => GetType().Name;

    private void LogInformation(int id, string message)
        => _logger.LogInformation(new EventId(id), $"{ControllerName} | {message}");

    private void LogError(int id, string message, Exception ex)
        => _logger.LogInformation(new EventId(id), ex, $"{ControllerName} | {message}");


    [Description("Gets data directly from scrapping website")]
    [HttpPost("scrape")]
    public async Task<IEnumerable<TResult>> Scrape([FromBody] TScrapeQuery scrapeQuery, CancellationToken token) =>
        await ScrapeInternal(scrapeQuery, token, 0) ?? Enumerable.Empty<TResult>();


    [Description("Gets data directly from scrapping website and repeats untill sucssess or timeout")]
    [HttpPost("scrapewait")]
    public async Task<IEnumerable<TResult>> ScrapeWait([FromBody] TScrapeQuery scrapeQuery, CancellationToken token)
        => await ScrapeWaitInternal(scrapeQuery, token, 1) ?? Enumerable.Empty<TResult>();

    [Description("Updates database cache without wait")]
    [HttpPost("update")]
    public void Update([FromBody] TUpdateQuery updateQuery)
    {
        const int eventId = 2;
        var scrapeQueries = ConvertToScrapeQueriesInternal(updateQuery);
        var cancellationTokenSource = new CancellationTokenSource(ScrapeWaitTimeout);
        var token = cancellationTokenSource.Token;
        foreach (var scrapeQuery in scrapeQueries)
            Task.Run(async () => await UpdateWaitInternal(scrapeQuery, updateQuery.Measurement, token, eventId), token);
    }

    [Description("Updates database cache and waits for completion")]
    [HttpPost("updatewait")]
    public async Task UpdateWait([FromBody] TUpdateQuery updateQuery, CancellationToken token) => 
        await UpdateWaitAllInternal(updateQuery, token, 3);

   

    [Description("Updates database cache and waits for completion")]
    [HttpPost("updatewaitonce")]
    public async Task UpdateWaitOnce([FromBody] TUpdateQuery updateQuery, CancellationToken token)
    {
        const int eventId = 4;
        await UpdateWaitOnceInternal(updateQuery, token, eventId);
    }

    
    [Description("Reads cached data")]
    [HttpPost("read")]
    public async Task<IEnumerable<TResult>> ReadCache([FromBody] TReadQuery readQuery,
        CancellationToken token) =>
        await ReadInternal(readQuery, false, token, 5) ?? Enumerable.Empty<TResult>();



    [Description(
        "Checks if data was updated in last 15 minutes, if not updates data, if fails schedule updating and reads cached data")]
    [HttpPost("updateread")]
    public async Task<IEnumerable<TResult>> UpdateOnceReadCache([FromBody] TReadQuery readQuery,
        CancellationToken token)
    {
        const int eventId = 6;
        var cache = (await ReadInternal(readQuery, true, token, eventId))?.ToArray();
        if (cache is not null && cache.FirstOrDefault() is { } top)
        {
            LogInformation(eventId,
                $"Top Result: {top}, time: {top.TimeWritten}, minTime {DateTime.Now.Subtract(ChacheValiditySpan)}, is cached: {top.TimeWritten >= DateTime.Now.Subtract(ChacheValiditySpan)}");
        }
        else
        {
            LogInformation(eventId, "cache empty");
        }
        
        LogInformation(eventId, $"TopResult: ");
        DateTime? lastFound = null;
        if (cache is not null && cache.FirstOrDefault() is { } topResult)
        {
            if(topResult.TimeWritten >= DateTime.Now.Subtract(ChacheValiditySpan) &&
               await ReadInternal(readQuery, false, token, eventId) is { } result)
                return result;
            lastFound = topResult.TimeWritten;
        }
                              
        var updateQuery = ConvertToUpdateQueryInternal(readQuery, lastFound);
        await UpdateWaitOnceInternal(updateQuery, token, eventId);
        var results = await ReadInternal(readQuery, false, token, eventId) ?? Enumerable.Empty<TResult>();
        return results;
    }


    private async Task<IEnumerable<TResult>?> ScrapeInternal(TScrapeQuery scrapeQuery, CancellationToken token,
        int eventId)
    {
        LogInformation(eventId, "Scrape started");

        try
        {
            return await ScrapeInternal(scrapeQuery, token);
        }
        catch (OperationCanceledException ex)
        {
            LogError(eventId, "Scrape canceled", ex);
        }
        catch (Exception ex)
        {
            LogError(eventId, "Scrape error", ex);
        }

        return null;
    }


    private async Task<IEnumerable<TResult>?> ScrapeWaitInternal(TScrapeQuery scrapeQuery, CancellationToken token,
        int eventId)
    {
        var endTime = DateTime.Now + ScrapeWaitTimeout;
        var retries = 0;

        LogInformation(eventId, "Scrape wait started");

        while (!token.IsCancellationRequested && DateTime.Now <= endTime)
        {
            try
            {
                return await ScrapeInternal(scrapeQuery, token);
            }
            catch (OperationCanceledException ex)
            {
                LogError(eventId, "Scrape wait canceled", ex);
                return null;
            }
            catch (Exception ex)
            {
                LogError(eventId, $"Scrape wait retrying {++retries}", ex);
            }

            try
            {
                await Task.Delay(ScrapeDelayTime, token);
            }
            catch (OperationCanceledException ex)
            {
                LogError(eventId, "Scrape wait canceled", ex);
                return Enumerable.Empty<TResult>();
            }
        }

        LogInformation(eventId, "Scrape wait timeout");
        return null;
    }

    private async Task<bool> UpdateInternal(TScrapeQuery scrapeQuery, string measurement, CancellationToken token,
        int eventId)
    {
        LogInformation(eventId, "Update started");

        try
        {
            var results = (await ScrapeInternal(scrapeQuery, token)).ToArray();
            if (!results.Any())
            {
                LogInformation(eventId, "Nothing to update");
                return false;
            }

            LogInformation(eventId, "Influx upload started");
            await _influxManager.Upload(results, measurement, token);
            return true;
        }
        catch (OperationCanceledException ex)
        {
            LogError(eventId, "Update canceled", ex);
        }
        catch (Exception ex)
        {
            LogError(eventId, "Update error", ex);
        }

        return false;
    }
    
    private async Task UpdateWaitOnceInternal(TUpdateQuery updateQuery, CancellationToken token, int eventId)
    {
        var scrapeQueries = ConvertToScrapeQueriesInternal(updateQuery).ToArray();

        var tasks = new Task<bool>[scrapeQueries.Length];
        for (var i = 0; i < scrapeQueries.Length; i++)
            tasks[i] = UpdateInternal(scrapeQueries[i], updateQuery.Measurement, token, eventId);

        var results = await Task.WhenAll(tasks);

        if (results.All(r => r))
            return;

        var cancellationTokenSource = new CancellationTokenSource(ScrapeDelayTime);
        var scheduleToken = cancellationTokenSource.Token;
        for (var i = 0; i < scrapeQueries.Length; i++)
        {
            if (results[i]) //Skip successful updates 
                continue;
            var scrapeQuery = scrapeQueries[i];
            _ = Task.Run(async () => await UpdateWaitInternal(scrapeQuery, updateQuery.Measurement, scheduleToken, eventId),
                scheduleToken);
        }
    }


    private async Task<bool> UpdateWaitInternal(TScrapeQuery scrapeQuery, string measurement, CancellationToken token,
        int eventId)
    {
        LogInformation(eventId, "Update started");
        try
        {
            var results = (await ScrapeWaitInternal(scrapeQuery, token, eventId))?.ToArray();
            if (results is null)
            {
                LogInformation(eventId, "Failed to get scrape data for update");
                return false;
            }

            if (!results.Any())
            {
                LogInformation(eventId, "Nothing to update");
                return false;
            }

            LogInformation(eventId, "Influx upload started");
            await _influxManager.Upload(results, measurement, token);
            return true;
        }
        catch (OperationCanceledException ex)
        {
            LogError(eventId, "Update canceled", ex);
        }
        catch (Exception ex)
        {
            LogError(eventId, "Update error", ex);
        }

        return false;
    }

    private async Task UpdateWaitAllInternal(TUpdateQuery updateQuery, CancellationToken token, int eventId)
    {
        var scrapeQueries = ConvertToScrapeQueriesInternal(updateQuery);
        var updateWaitTasks = scrapeQueries.Select(scrapeQuery =>
            UpdateWaitInternal(scrapeQuery, updateQuery.Measurement, token, eventId));
        var results = await Task.WhenAll(updateWaitTasks);
        if (results.All(r => !r))
            LogInformation(eventId, "Update wait failed");
        else if (results.All(r => r))
            LogInformation(eventId, "Update wait partially failed");
        else
            LogInformation(eventId, "Update wait succeeded");
    }
    
    private async Task<IEnumerable<TResult>?> ReadInternal(TReadQuery scrapeQuery, bool singleResult, CancellationToken token,
        int eventId)
    {
        LogInformation(eventId, "Read started");

        try
        {
            var query = scrapeQuery.ToQuery(singleResult);
            return await _influxManager.Query<TResult>(query, token);
        }
        catch (OperationCanceledException ex)
        {
            LogError(eventId, "Read canceled", ex);
        }
        catch (Exception ex)
        {
            LogError(eventId, "Read error", ex);
        }

        return null;
    }
}
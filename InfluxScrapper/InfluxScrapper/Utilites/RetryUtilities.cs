using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Stock;

namespace InfluxScrapper.Utilites;

public static class RetryUtilities
{
    private static readonly TimeSpan RetryWaitTime = TimeSpan.FromMinutes(1);
    private static readonly TimeSpan ScheduleWaitTime = TimeSpan.FromMinutes(60);
    
    
    public static async Task<IEnumerable<T>?> Scrape<T>(ILogger logger, Func<Task<IEnumerable<T>?>> generateTask,
        CancellationToken token)
    {
        try
        {
            return await generateTask();
        }
        catch (Exception ex)
        {
            logger.LogError(new EventId(1), ex, "Scrape Error");
        }

        return null;
    }
    
    public static async Task<IEnumerable<T>?> ScrapeRetry<T>(ILogger logger, Func<Task<IEnumerable<T>?>> generateTask,
        CancellationToken token)
    {
        while (true)
        {
            var result = await Scrape(logger, generateTask, token);
            if (result is not null)
                return result;
            
            try
            {
                logger.LogInformation("Retrying");

                await Task.Delay(RetryWaitTime, token);
            }
            catch (TaskCanceledException)
            {
                logger.LogInformation(new EventId(3), "Scraping Canceled");
                return null;
            }
        }
    }

    public static async Task<bool> Update<T>(ILogger logger, Task<IEnumerable<T>?> scrape,
        Func<T, PointData> convert, CancellationToken token)
    {
        try
        {
            var results = await scrape;
            if (results is null)
                return false;
            await InfluxDBUtilites.UploadData(results, convert, token);
            logger.LogInformation("Writing done");
            return true;
        }
        catch (Exception ex)
        {
            logger.LogError(new EventId(0), ex, "Update exception");
            return false;
        }
    }

    public static async Task<IEnumerable<T>> Query<T>(ILogger logger, string query, Func<FluxRecord, T> convert, CancellationToken token)
    {
        try
        {
            var result = await InfluxDBUtilites.ParseQuery(query, convert, token);
            return result;
        }
        catch(Exception ex)
        {
            logger.LogError(new EventId(5), ex, "Cache query exception");
            return Enumerable.Empty<T>();
        }
    }

    public static async Task<bool[]> UpdateWaitOnceOrScheduleUpdate(ILogger logger, CancellationToken token, params Func<CancellationToken, Task<bool>>[] tasks)
    {
        var runTasks = tasks.Select(t => t(token));
        var results = await Task.WhenAll(runTasks);
        for (int i = 0; i < results.Length; i++)
        {
            if (results[i])
                continue;
            ScheduleUpdate(logger, tasks[i]);
        }

        return results;
    }
    
    public static async Task<bool[]> UpdateWaitAll(ILogger logger, CancellationToken token, params Func<CancellationToken, Task<bool>>[] tasks)
    {
        var updateWaitTasks =  tasks.Select(t => UpdateWait(logger, token, t));
        var results = await Task.WhenAll(updateWaitTasks);
        return results;
    }
    
    public static async Task<bool> UpdateWait(ILogger logger, CancellationToken token, Func<CancellationToken, Task<bool>> task)
    {
        while (!token.IsCancellationRequested)
        {
            var result = await task(token);
            if (result)
                return true;
            
            try
            {
                logger.LogInformation("Retrying");
                await Task.Delay(RetryWaitTime, token);
            }
            catch (TaskCanceledException)
            {
                logger.LogInformation(new EventId(4), "Updating Canceled");
                return false;
            }
        }

        return false;
    }

    public static void ScheduleUpdates(ILogger logger, params Func<CancellationToken, Task<bool>>[] tasks)
    {
        foreach (var task in tasks)
            ScheduleUpdate(logger, task);
    }
    
    public static void ScheduleUpdate(ILogger logger, Func<CancellationToken, Task<bool>> task)
    {
        var cancellationTokenSource = new CancellationTokenSource(ScheduleWaitTime);
        var token = cancellationTokenSource.Token;
        Task.Run(async () => await UpdateWait(logger,token , task), token);
    }
    
}
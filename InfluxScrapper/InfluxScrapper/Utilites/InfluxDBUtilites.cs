using System.Drawing;
using System.Text;
using InfluxDB.Client;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Quote;
using InfluxScrapper.Models.Stock;

namespace InfluxScrapper.Utilites;

public static class InfluxDBUtilites
{
    public static string ConstructQuery(StockCacheQuery query, bool onlyLastResult = false)
    {
        var builder = new StringBuilder();
        AppendQueryBase(builder);
        AppendTimeRange(query.TimeFrom, query.TimeTo, builder);
        AppendMeasurementFilter(builder, query.Measurement);
        AppendFilter(builder, "ticker", query.Symbol);
        if (onlyLastResult)
            AppendQueryLastResult(builder);
        AppendQueryEnd(builder);
        return builder.ToString();
    }
    
    public static string ConstructQuery(StockQuoteCacheQuery cacheQuery, bool onlyLastResult = true)
    {
        var builder = new StringBuilder();
        AppendQueryBase(builder);
        AppendTimeRange(cacheQuery.TimeFrom, cacheQuery.TimeTo, builder);
        AppendMeasurementFilter(builder, cacheQuery.Measurement2);
        AppendOrFilter(builder, cacheQuery.Symbols.Select(s => ("ticker", s)).ToArray());
        if (onlyLastResult)
            AppendQueryLastResult(builder);
        AppendQueryEnd(builder);
        return builder.ToString();
    }
    

    public static string ConstructQuery(ForexExchangeRateCacheQuery cacheCacheQuery, bool onlyLastResult = true)
    {
        var builder = new StringBuilder();
        AppendQueryBase(builder);
        AppendTimeRange(cacheCacheQuery.TimeFrom, cacheCacheQuery.TimeTo, builder);
        AppendMeasurementFilter(builder, cacheCacheQuery.Measurement);
        AppendOrFilter2(builder,
            cacheCacheQuery.Currencies.Select(s => (("from", s.SymbolFrom), ("to", s.SymbolTo))).ToArray());
        if (onlyLastResult)
            AppendQueryLastResult(builder);
        AppendQueryEnd(builder);
        return builder.ToString();
    }




    private static void AppendQueryBase(StringBuilder builder)
    {
        builder.AppendLine("import \"influxdata/influxdb/schema\"");
        builder.AppendLine($"from(bucket:\"{Constants.InfluxBucket}\")");
    }


    private static void AppendMeasurementFilter(StringBuilder builder, string measurement) 
        => AppendFilter(builder, "_measurement", measurement);
    
    private static void AppendFilter(StringBuilder builder, string key, string value) 
        => builder.AppendLine($"|> filter(fn: (r) =>  r[\"{key}\"] == \"{value}\")");

    private static void AppendAndFilter(StringBuilder builder, params (string Key, string Value)[] checks)
    {
        builder.Append($"|> filter(fn: (r) => ");
        var first = true;
        foreach (var (key, value) in checks)
        {
            builder.Append(first ? " " : " and ");
            builder.Append($" r[\"{key}\"] == \"{value}\" ");
            first = false;
        }

        builder.AppendLine(")");
    }
    
    private static void AppendOrFilter(StringBuilder builder, params (string Key, string Value)[] checks)
    {
        builder.Append($"|> filter(fn: (r) => ");
        var first = true;
        foreach (var (key, value) in checks)
        {
            builder.Append(first ? " " : " or ");
            builder.Append($" r[\"{key}\"] == \"{value}\" ");
            first = false;
        }

        builder.AppendLine(")");
    }
    
    private static void AppendOrFilter2(StringBuilder builder, params ((string Key, string Value) p1,(string Key, string Value) p2)[] checks)
    {
        builder.Append($"|> filter(fn: (r) => ");
        var first = true;
        foreach (var ((key1, value1),(key2, value2)) in checks)
        {
            builder.Append(first ? " " : " or ");
            builder.Append($" r[\"{key1}\"] == \"{value1}\" and r[\"{key2}\"] == \"{value2}\" ");
            first = false;
        }

        builder.AppendLine(")");
    }
    
    private static void AppendTimeRange(DateTime? timeFrom, DateTime? timeTo, StringBuilder builder)
    {
        if (timeFrom is not null && timeTo is not null)
            builder.AppendLine($"|> range(start: {DateTime.SpecifyKind(timeFrom.Value, DateTimeKind.Utc):o}, " +
                               $"stop: {DateTime.SpecifyKind(timeTo.Value, DateTimeKind.Utc):o})");
        else if (timeFrom is not null)
            builder.AppendLine($"|> range(start: {DateTime.SpecifyKind(timeFrom.Value, DateTimeKind.Utc):o})");
        else
            builder.AppendLine("|> range(start: 0)");
    }
    
    private static void AppendQueryLastResult(StringBuilder builder) => builder.AppendLine("|> last()");
    
    private static void AppendQueryEnd(StringBuilder builder) => builder.AppendLine("|> schema.fieldsAsCols()");


    public static async Task<IEnumerable<T>> ParseQuery<T>(string query, Func<FluxRecord, T> func,
        CancellationToken token)
    {
        using var client = InfluxDBClientFactory.Create(Constants.InfluxDBUrl, Constants.InfluxToken);
        var queryApi = client.GetQueryApi();
        var tables = await queryApi.QueryAsync(query, Constants.InfluxOrg, token);
        return tables.SelectMany(table => table.Records.Select(func));

    }
    
    public static async Task UploadData<T>(IEnumerable<T> items, Func<T, PointData> func,
        CancellationToken token)
    {
        using var client = InfluxDBClientFactory.Create(Constants.InfluxDBUrl, Constants.InfluxToken);
        var writeApi = client.GetWriteApiAsync();
        var points = items.Select(func).ToArray();
        await writeApi.WritePointsAsync(points, Constants.InfluxBucket, Constants.InfluxOrg, token);
    }
}
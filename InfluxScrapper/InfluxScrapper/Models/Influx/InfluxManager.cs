using System.Diagnostics.CodeAnalysis;
using InfluxDB.Client;
using InfluxScrapper.Models.Influx;

namespace InfluxScrapper.Influx;

public class InfluxManager : IInfluxManager
{
    private readonly string _url;
    private readonly string _token;
    private readonly string _org;
    private readonly string _bucket;

    public InfluxManager(string url, string token, string org, string bucket)
    {
        _url = url;
        _token = token;
        _org = org;
        _bucket = bucket;
    }
    
    public async Task<IEnumerable<T>> Query<T>(string query, CancellationToken token) where T : InvfluxRecord<T>
    {
        using var client = InfluxDBClientFactory.Create(_url, _token);
        var queryApi = client.GetQueryApi();
        var tables = await queryApi.QueryAsync(query, _org, token);
        return tables.SelectMany(table => table.Records.Select(T.FromRecord));
    }
    
    public  async Task Upload<T>(IEnumerable<T> records, string measurement, CancellationToken token) where T : InvfluxRecord<T>
    {
        using var client = InfluxDBClientFactory.Create(_url, _token);
        var writeApi = client.GetWriteApiAsync();
        var points = records.Select(r=>T.ToPointData(r, measurement)).ToArray();
        await writeApi.WritePointsAsync(points, _bucket, _org, token);
    }
}
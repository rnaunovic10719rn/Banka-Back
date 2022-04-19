namespace InfluxScrapper.Models.Influx;

public interface IInfluxManager
{
    Task<IEnumerable<T>> Query<T>(string query, CancellationToken token) where T : InvfluxRecord<T>;
    Task Upload<T>(IEnumerable<T> records, string measurement, CancellationToken token)
        where T : InvfluxRecord<T>;
}
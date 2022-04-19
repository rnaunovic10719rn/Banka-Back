namespace InfluxScrapper.Models.Influx;

public interface InfluxCacheQuery<T> : IInfluxMeasurementHolder
{
    public DateTime? TimeFrom { get; init; }
    public DateTime? TimeTo { get; init; }

    public string ToQuery(bool singleFile = false);
}
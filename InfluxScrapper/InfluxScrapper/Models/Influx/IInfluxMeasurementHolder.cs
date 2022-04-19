namespace InfluxScrapper.Models.Influx;

public interface IInfluxMeasurementHolder
{
    string Measurement { get; }
}
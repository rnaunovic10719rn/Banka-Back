using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;

namespace InfluxScrapper.Models.Influx;

public interface InvfluxRecord<TSelf>
{
    public DateTime Time { get; set; }
    static abstract PointData ToPointData(TSelf item, string measurement);
    static abstract TSelf FromRecord(FluxRecord record);
}
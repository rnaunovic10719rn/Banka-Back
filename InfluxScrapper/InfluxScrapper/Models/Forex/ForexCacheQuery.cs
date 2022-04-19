using InfluxScrapper.Models.Influx;
using InfluxScrapper.Utilites;

namespace InfluxScrapper.Models.Forex;

public class ForexCacheQuery : ForexQuery , InfluxCacheQuery<ForexCacheQuery>
{
    public DateTime? TimeFrom { get; init; }
    public DateTime? TimeTo { get; init; }
    public string ToQuery(bool singleFile = false) => InfluxDBUtilites.ConstructQuery(this, singleFile);

}
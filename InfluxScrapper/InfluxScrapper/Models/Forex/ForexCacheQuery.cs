using InfluxScrapper.Models.Influx;
using InfluxScrapper.Utilities;

namespace InfluxScrapper.Models.Forex;

public class ForexCacheQuery : ForexQuery , InfluxCacheQuery<ForexCacheQuery>
{
    public DateTime? TimeFrom { get; init; }
    public DateTime? TimeTo { get; init; }
    public string ToQuery(bool singleFile = false) => InfluxDbUtilities.ConstructQuery(this, singleFile);

}

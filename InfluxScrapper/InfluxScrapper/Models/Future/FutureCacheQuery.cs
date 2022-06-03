using InfluxScrapper.Models.Influx;
using InfluxScrapper.Utilities;

namespace InfluxScrapper.Models.Future;

public class FutureCacheQuery : FutureQuery, InfluxCacheQuery<FutureCacheQuery>
{
    public DateTime? TimeFrom { get; init; }
    public DateTime? TimeTo { get; init; }
    public string ToQuery(bool singleFile = false) => InfluxDbUtilities.ConstructQuery(this, true);
}

using System.Text;
using InfluxScrapper.Models.Influx;
using InfluxScrapper.Utilities;

namespace InfluxScrapper.Models.Stock;

public class StockCacheQuery : StockQuery, InfluxCacheQuery<StockCacheQuery> 
{
    public StockCacheQuery()
    {
        
    }
    public StockCacheQuery(StockQuery query, DateTime? timeFrom = null, DateTime? timeTo = null)
    {
        Type = query.Type;
        Symbol = query.Symbol;
        Interval = query.Interval;
        TimeFrom = timeFrom;
        TimeTo = timeTo;
    }
    public DateTime? TimeFrom { get; init; }
    public DateTime? TimeTo { get; init; }

    public string ToQuery(bool singleFile = false) => InfluxDbUtilities.ConstructQuery(this, singleFile);
}
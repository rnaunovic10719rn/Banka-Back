namespace InfluxScrapper.Models.Stock;

public class StockCacheQuery : StockQuery
{
    public StockCacheQuery()
    {
        
    }
    public StockCacheQuery(StockQuery query, DateTime? timeFrom = null, DateTime? timeTo = null)
    {
        Type = query.Type;
        Symbol = query.Symbol;
        Interval = query.Interval;
        
    }
    public DateTime? TimeFrom { get; set; }
    public DateTime? TimeTo { get; set; }
    
}
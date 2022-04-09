namespace InfluxScrapper.Models.Stock;

public class StockCacheQuery : StockQuery
{
    public DateTime? TimeFrom { get; set; }
    public DateTime? TimeTo { get; set; }
}
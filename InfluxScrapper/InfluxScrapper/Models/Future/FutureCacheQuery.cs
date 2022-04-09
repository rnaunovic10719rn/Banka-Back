namespace InfluxScrapper.Models.Future;

public class FutureCacheQuery : FutureQuery
{
    public DateTime? TimeFrom { get; set; }
    public DateTime? TimeTo { get; set; }
}
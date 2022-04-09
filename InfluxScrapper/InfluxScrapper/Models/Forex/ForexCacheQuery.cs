using System.ComponentModel;
using System.ComponentModel.DataAnnotations;

namespace InfluxScrapper.Models.Forex;

public class ForexCacheQuery : ForexQuery
{
    public DateTime? TimeFrom { get; set; }
    public DateTime? TimeTo { get; set; }
}
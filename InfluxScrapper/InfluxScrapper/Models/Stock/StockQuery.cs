using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using InfluxScrapper.Models.Influx;

namespace InfluxScrapper.Models.Stock;

public abstract class StockQuery : IInfluxMeasurementHolder
{
    [Required]
    public StockType Type { get; set; }
    
    [Required]
    [RegularExpression("^[a-zA-Z]{2,6}$")]
    [DefaultValue("aapl")]
    public string Symbol { get; set; }
    
    [RegularExpression("^(1|5|15|30|60)min$")]
    [DefaultValue("60min")]
    public string? Interval { get; set; }
    
    public string Measurement => $"stock_{Type.ToString().ToLower()}{(Type == StockType.Intraday ? $"_{Interval ?? "60min"}" : "")}";
}
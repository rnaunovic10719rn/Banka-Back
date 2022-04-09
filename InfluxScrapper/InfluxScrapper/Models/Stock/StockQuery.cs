using System.ComponentModel;
using System.ComponentModel.DataAnnotations;

namespace InfluxScrapper.Models.Stock;

public abstract class StockQuery
{
    [Required]
    public StockType Type { get; set; }
    
    [Required]
    [RegularExpression("^[a-zA-Z]{3,6}$")]
    [DefaultValue("aapl")]
    public string Symbol { get; set; }
    
    [RegularExpression("^(1|5|15|30|60)min$")]
    [DefaultValue("1min")]
    public string? Interval { get; set; }

}
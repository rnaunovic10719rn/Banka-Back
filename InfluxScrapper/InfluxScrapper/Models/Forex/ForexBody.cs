using System.ComponentModel;
using System.ComponentModel.DataAnnotations;

namespace InfluxScrapper.Models.Forex;

public class ForexBody
{
    [Required]
    [RegularExpression("^[a-zA-Z]{2,6}$")]
    [DefaultValue("usd")]
    public string SymbolTo { get; set; } = "";
    
    [Required]
    [RegularExpression("^[a-zA-Z]{2,6}$")]
    [DefaultValue("eur")]
    public string SymbolFrom { get; set; } = "";
}
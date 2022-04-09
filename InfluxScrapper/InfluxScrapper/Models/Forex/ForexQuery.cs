using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.Text;

namespace InfluxScrapper.Models.Forex;

public class ForexQuery
{
    [Required]
    public ForexType Type { get; set; }
    
    [Required]
    [RegularExpression("^[a-zA-Z]{3,6}$")]
    [DefaultValue("usd")]
    public string SymbolTo { get; set; }
    
    [Required]
    [RegularExpression("^[a-zA-Z]{3,6}$")]
    [DefaultValue("eur")]
    public string SymbolFrom { get; set; }
    
    [RegularExpression("^(1|5|15|30|60)min$")]
    [DefaultValue("1min")]
    public string? Interval { get; set; }

    public string Function => Type switch
    {
        ForexType.Intraday => "FX_INTRADAY",
        ForexType.Daily => "FX_DAILY",
        ForexType.Weekly => "FX_WEEKLY",
        ForexType.Monthly => "FX_MONTHLY",
        _ => throw new ArgumentOutOfRangeException()
    };
    
    public string Url
    {
        get
        {
            var builder = new StringBuilder();
            builder.Append($"https://www.alphavantage.co/query?function={Function}");
            builder.Append($"&from_symbol={SymbolFrom}&to_symbol={SymbolTo}&apikey={Constants.AlphaVantageApiKey}&datatype=csv");
            if (Type == ForexType.Intraday)
                builder.Append($"&interval={Interval ?? "60min" }&outputsize=full");
            else if (Type == ForexType.Daily)
                builder.Append("outputsize=full");
            return builder.ToString();
        }
    }

    public string Measurement => $"stock_{Type.ToString().ToLower()}_{SymbolFrom.ToLower()}_{SymbolTo.ToLower()}";
    
}
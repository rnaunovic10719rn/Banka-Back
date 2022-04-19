using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.Text;

namespace InfluxScrapper.Models.Quote;

public class StockQuoteQuery
{
    [Required]
    [RegularExpression("^[a-zA-Z]{2,6}$")]
    [DefaultValue("aapl")]
    public string Symbol { get; set; } = "";

    public string Measurement => "stock_quote";
    
    public string Url
    {
        get
        {
            var builder = new StringBuilder();
            builder.Append($"https://www.alphavantage.co/query?function=GLOBAL_QUOTE");
            builder.Append($"&symbol={Symbol}&apikey={Constants.AlphaVantageApiKey}");
            builder.Append("&datatype=csv");
            return builder.ToString();
        }
    }
    
}
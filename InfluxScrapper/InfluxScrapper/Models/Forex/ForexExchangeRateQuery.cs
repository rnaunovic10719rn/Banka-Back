using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.Text;
using NodaTime;

namespace InfluxScrapper.Models.Stock;

public class ForexExchangeRateQuery
{
    [Required]
    [RegularExpression("^[a-zA-Z]{2,6}$")]
    [DefaultValue("usd")]
    public string FromCurrency { get; set; }
    
    
    [Required]
    [RegularExpression("^[a-zA-Z]{2,6}$")]
    [DefaultValue("eur")]
    public string ToCurrency { get; set; }

    
    public string Measurement => "forex_exchange_rates";
    
    public string Url
    {
        get
        {
            var builder = new StringBuilder();
            builder.Append($"https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE");
            builder.Append($"&from_currency={FromCurrency}&to_currency={ToCurrency}&apikey={Constants.AlphaVantageApiKey}");
            builder.Append("&datatype=csv");
            return builder.ToString();
        }
    }
    
}
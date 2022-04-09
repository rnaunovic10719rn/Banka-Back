using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.Text;

namespace InfluxScrapper.Models.Stock;

public class StockScrapeQuery : StockQuery
{
    public StockScrapeQuery()
    {
        
    }
    public StockScrapeQuery(StockQuery query, int? month = null, int? year = null)
    {
        Month = month;
        Year = year;
        Type = query.Type;
        Symbol = query.Symbol;
        Interval = query.Interval;
    }
    
    [Range(1, 12)]
    [DefaultValue(1)]
    public int? Month { get; set; }
    
    [Range(1, 20)]
    [DefaultValue(1)]
    public int? Year { get; set; }
    

    public string Slice => $"year{(Year is > 1 ? 2 : 1)}month{Month ?? 1}";
    public string Function => Type switch
    {
        StockType.Intraday => "TIME_SERIES_INTRADAY_EXTENDED",
        StockType.Daily => "TIME_SERIES_DAILY",
        StockType.Weekly => "TIME_SERIES_WEEKLY",
        StockType.Monthly => "TIME_SERIES_MONTHLY",
        _ => throw new ArgumentOutOfRangeException()
    };
    
    public string Url
    {
        get
        {
            var builder = new StringBuilder();
            builder.Append($"https://www.alphavantage.co/query?function={Function}");
            builder.Append($"&symbol={Symbol}&apikey={Constants.AlphaVantageApiKey}");
            if (Type == StockType.Intraday)
                builder.Append($"&interval={Interval ?? "60min" }&slice={Slice}&adjusted=false");
            else
            {
                builder.Append("&datatype=csv");
                if (Type == StockType.Daily)
                    builder.Append("outputsize=full");
            }

            return builder.ToString();
        }
    }

    public string Measurement => $"stock_{Type.ToString().ToLower()}";
}
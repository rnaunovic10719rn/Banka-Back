using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.Text;
using InfluxScrapper.Models.Influx;

namespace InfluxScrapper.Models.Stock;

public class StockQuoteCacheQuery : InfluxCacheQuery<StockQuoteCacheQuery>
{
    [Required]
    [DefaultValue(new [] {"aapl", "msft"})]
    public string[] Symbols { get; set; }

    public DateTime? TimeFrom { get; init; } = null;
    public DateTime? TimeTo { get; init; } = null;
    public string Measurement  => "stock_quote";
    public string BuildFilter(StringBuilder builder)
    {
        throw new NotImplementedException();
    }

    public string ToQuery(bool singleFile = false)
    {
        throw new NotImplementedException();
    }

    public string Measurement2 => "stock_quote";

    public IEnumerable<StockQuoteQuery> ToQuotes() => Symbols.Select(s => new StockQuoteQuery() {Symbol = s});

}
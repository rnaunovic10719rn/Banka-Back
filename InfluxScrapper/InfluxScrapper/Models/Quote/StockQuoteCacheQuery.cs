using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.Text;
using InfluxScrapper.Models.Influx;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilities;

namespace InfluxScrapper.Models.Quote;

public class StockQuoteCacheQuery : InfluxCacheQuery<StockQuoteCacheQuery>
{
    [Required]
    [DefaultValue(new[] {"aapl", "msft"})]
    public string[] Symbols { get; set; } = Array.Empty<string>();

    public DateTime? TimeFrom { get; init; } = null;
    public DateTime? TimeTo { get; init; } = null;
    public string Measurement  => "stock_quote";

    public string ToQuery(bool singleFile = false) => InfluxDbUtilities.ConstructQuery(this, true);

    public IEnumerable<StockQuoteQuery> ToQuotes() => Symbols.Select(s => new StockQuoteQuery() {Symbol = s});

}

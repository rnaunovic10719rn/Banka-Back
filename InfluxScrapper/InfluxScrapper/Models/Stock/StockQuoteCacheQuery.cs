using System.ComponentModel;
using System.ComponentModel.DataAnnotations;

namespace InfluxScrapper.Models.Stock;

public class StockQuoteCacheQuery
{
    [Required]
    [RegularExpression("^[a-zA-Z]{2,6}$")]
    [DefaultValue(new [] {"aapl", "msft"})]
    public string[] Symbols { get; set; }
    public DateTime? TimeFrom { get; set; }
    public DateTime? TimeTo { get; set; }

    public string Measurement => "stock_quote";

    public IEnumerable<StockQuoteQuery> ToQuotes() => Symbols.Select(s => new StockQuoteQuery() {Symbol = s});

}
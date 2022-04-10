using System.ComponentModel;
using System.ComponentModel.DataAnnotations;

namespace InfluxScrapper.Models.Stock;

public class StockQuoteCacheQuery
{
    [Required]
    [DefaultValue(new [] {"aapl", "msft"})]
    public string[] Symbols { get; set; }

    public DateTime? TimeFrom => null;//{ get; set; }
    public DateTime? TimeTo => null; //{ get; set; }

    public string Measurement => "stock_quote";

    public IEnumerable<StockQuoteQuery> ToQuotes() => Symbols.Select(s => new StockQuoteQuery() {Symbol = s});

}
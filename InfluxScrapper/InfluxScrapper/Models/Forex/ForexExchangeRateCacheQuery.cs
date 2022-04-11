using System.ComponentModel.DataAnnotations;
using InfluxScrapper.Models.Forex;

namespace InfluxScrapper.Models.Stock;

public class ForexExchangeRateCacheQuery
{
    [Required]
    public ForexBody[] Currencies { get; set; }

    public DateTime? TimeFrom => null;//{ get; set; }
    public DateTime? TimeTo => null; //{ get; set; }

    public string Measurement => "forex_exchange_rates";

    public IEnumerable<ForexExchangeRateQuery> ToQuotes() => Currencies.Select(s => new ForexExchangeRateQuery()
    {
        FromCurrency = s.SymbolFrom,
        ToCurrency = s.SymbolTo
    });

}
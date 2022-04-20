using System.ComponentModel.DataAnnotations;
using InfluxScrapper.Models.Forex;
using InfluxScrapper.Models.Influx;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilites;

namespace InfluxScrapper.Models.Exchange;

public class ForexExchangeRateCacheQuery : InfluxCacheQuery<ForexExchangeRateCacheQuery>, IInfluxMeasurementHolder
{
    [Required] public ForexBody[] Currencies { get; set; } = Array.Empty<ForexBody>();

    public DateTime? TimeFrom { get; init; }
    public DateTime? TimeTo { get; init; }

    public string ToQuery(bool singleFile = false) => InfluxDBUtilites.ConstructQuery(this, singleFile);

    public string Measurement => "forex_exchange_rates";

    public IEnumerable<ForexExchangeRateQuery> ToQuotes() => Currencies.Select(s => new ForexExchangeRateQuery()
    {
        FromCurrency = s.SymbolFrom,
        ToCurrency = s.SymbolTo
    });

}
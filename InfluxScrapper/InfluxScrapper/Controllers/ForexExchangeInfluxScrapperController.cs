using InfluxScrapper.Influx;
using InfluxScrapper.Models.Controllers;
using InfluxScrapper.Models.Exchange;
using InfluxScrapper.Models.Forex;
using InfluxScrapper.Models.Stock;
using InfluxScrapper.Utilites;
using Microsoft.AspNetCore.Mvc;
using NodaTime;
using NodaTime.Extensions;

namespace InfluxScrapper.Controllers;

[ApiController]
[Route("alphavantage/forex/exchangerate")]
public class ForexExchangeInfluxScrapperController : InfluxScrapperController<ForexExchangeRateCacheQuery, ForexExchangeRateQuery,
    ForexExchangeRateCacheQuery,
    ForexExchangeRateResult>
{
    public ForexExchangeInfluxScrapperController(IHttpClientFactory httpClientFactory,
        ILogger<ForexExchangeInfluxScrapperController>
            logger, InfluxManager influxManager) : base(httpClientFactory, logger, influxManager)
    {
    }

    internal override IEnumerable<ForexExchangeRateQuery> ConvertToScrapeQueriesInternal(
        ForexExchangeRateCacheQuery updateQuery)
        => updateQuery.ToQuotes();

    internal override ForexExchangeRateCacheQuery ConvertToUpdateQueryInternal(ForexExchangeRateCacheQuery readQuery)
        => readQuery;

    internal override async Task<IEnumerable<ForexExchangeRateResult>> ScrapeInternal(
        ForexExchangeRateQuery scrapeQuery,
        CancellationToken token)
    {
        var resultJson = await HttpUtilities.GetJSON<ForexExchangeRateJson>(scrapeQuery.Url, _httpClientFactory, token);
        if (resultJson is null)
            return null;
        var result = new ForexExchangeRateResult();
        result.Ask = resultJson.Body.Ask;
        result.Bid = resultJson.Body.Bid;
        result.ExchangeRate = resultJson.Body.ExchangeRate;
        result.FromCurrency = scrapeQuery.FromCurrency;
        result.ToCurrency = scrapeQuery.ToCurrency;
        return new[] {result};
    }
}
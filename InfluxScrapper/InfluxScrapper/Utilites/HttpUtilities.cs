using System.Globalization;
using CsvHelper;
using CsvHelper.Configuration;
using InfluxScrapper.Models.Exchange;
using InfluxScrapper.Models.Forex;
using InfluxScrapper.Models.Quote;
using InfluxScrapper.Models.Stock;

namespace InfluxScrapper.Utilites;

public static class HttpUtilities
{
    public static async Task<IEnumerable<StockResult>?> ScrapeStock(StockScrapeQuery query, IHttpClientFactory httpFactory)
    {
        /*var results = await GetCSV<StockResult>(query.Url, httpFactory);
        results?.ForEach(r => r.Ticker = query.Symbol);
        return results;*/
        return ArraySegment<StockResult>.Empty;
    }
    
    public static async Task<IEnumerable<StockQuoteResult>?> ScrapeStockQuote(StockQuoteQuery query, IHttpClientFactory httpFactory)
    {
        /*var results = await GetCSV<StockQuoteResult>(query.Url, httpFactory);
        results?.ForEach(r => r.Ticker = query.Symbol);
        return results;*/
        return  ArraySegment<StockQuoteResult>.Empty;
    }
    
    public static async Task<IEnumerable<ForexExchangeRateResult>?> ScrapeForexExchangeRate(ForexExchangeRateQuery query, IHttpClientFactory httpFactory)
    {
        return ArraySegment<ForexExchangeRateResult>.Empty;
        /*var resultJson = await GetJSON<ForexExchangeRateJson>(query.Url, httpFactory);

        if (resultJson is null) 
            return null;
        var result = new ForexExchangeRateResult();
        result.Ask = resultJson.Body.Ask;
        result.Bid = resultJson.Body.Bid;
        result.ExchangeRate = resultJson.Body.ExchangeRate;
        result.FromCurrency = query.FromCurrency;
        result.ToCurrency = query.ToCurrency;
        return new []{result};*/
    }
    
    
    public static async Task<List<T>?> GetCSV<T>(string url, IHttpClientFactory httpFactory, CancellationToken token)
    {
        var httpClient = httpFactory.CreateClient();
        var httpRequest = new HttpRequestMessage(HttpMethod.Get, url);
        var httpResponseMessage = await httpClient.SendAsync(httpRequest, token);
        if (!httpResponseMessage.IsSuccessStatusCode)
            return null;

        await using var stream = await httpResponseMessage.Content.ReadAsStreamAsync(token);
        var reader = new StreamReader(stream);
        using var csv = new CsvReader(reader,
            new CsvConfiguration(CultureInfo.InvariantCulture)
            {
                PrepareHeaderForMatch = args => args.Header.ToLower()
            });
        return csv.GetRecords<T>()?.ToList();
    }
    
    public static async Task<T?> GetJSON<T>(string url, IHttpClientFactory httpFactory, CancellationToken token) where T : class
    {
        var httpClient = httpFactory.CreateClient();
        var httpRequest = new HttpRequestMessage(HttpMethod.Get, url);
        var httpResponseMessage = await httpClient.SendAsync(httpRequest, token);
        if (!httpResponseMessage.IsSuccessStatusCode)
            return null;
        var result =  await httpResponseMessage.Content.ReadFromJsonAsync<T?>(cancellationToken: token);
        return result;
    }
}
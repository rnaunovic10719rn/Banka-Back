using System.Globalization;
using CsvHelper;
using CsvHelper.Configuration;
using InfluxScrapper.Models.Stock;

namespace InfluxScrapper.Utilites;

public static class AlphaVantageScrapper
{
    public static async Task<IEnumerable<StockResult>?> ScrapeStock(StockScrapeQuery query, IHttpClientFactory httpFactory)
    {
        var results = await ParseCSV<StockResult>(query.Url, httpFactory);
        results?.ForEach(r => r.Ticker = query.Symbol);
        return results;
    }
    
    public static async Task<IEnumerable<StockQuoteResult>?> ScrapeStockQuote(StockQuoteQuery query, IHttpClientFactory httpFactory)
    {
        var results = await ParseCSV<StockQuoteResult>(query.Url, httpFactory);
        results?.ForEach(r => r.Ticker = query.Symbol);
        return results;
    }
    
    public static async Task<IEnumerable<ForexExchangeRateResult>?> ScrapeForexExchangeRate(ForexExchangeRateQuery query, IHttpClientFactory httpFactory)
    {
        var result = await ParseJson<ForexExchangeRateResult>(query.Url, httpFactory);

        if (result is null) 
            return null;
        
        result.FromCurrency = query.FromCurrency;
        result.ToCurrency = query.ToCurrency;
        return new []{result};
    }
    
    
    private static async Task<List<T>?> ParseCSV<T>(string url, IHttpClientFactory httpFactory)
    {
        var httpClient = httpFactory.CreateClient();
        var httpRequest = new HttpRequestMessage(HttpMethod.Get, url);
        var httpResponseMessage = await httpClient.SendAsync(httpRequest);
        if (!httpResponseMessage.IsSuccessStatusCode)
            return null;

        await using var stream = await httpResponseMessage.Content.ReadAsStreamAsync();
        var reader = new StreamReader(stream);
        using var csv = new CsvReader(reader,
            new CsvConfiguration(CultureInfo.InvariantCulture)
            {
                PrepareHeaderForMatch = args => args.Header.ToLower()
            });
        return csv.GetRecords<T>()?.ToList();
    }
    
    private static async Task<T?> ParseJson<T>(string url, IHttpClientFactory httpFactory) where T : class
    {
        var httpClient = httpFactory.CreateClient();
        var httpRequest = new HttpRequestMessage(HttpMethod.Get, url);
        var httpResponseMessage = await httpClient.SendAsync(httpRequest);
        if (!httpResponseMessage.IsSuccessStatusCode)
            return null;
        var result =  await httpResponseMessage.Content.ReadFromJsonAsync<T?>();
        return result;
    }
}
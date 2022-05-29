using System.Globalization;
using CsvHelper;
using CsvHelper.Configuration;
using InfluxScrapper.Models.Exchange;
using InfluxScrapper.Models.Forex;
using InfluxScrapper.Models.Quote;
using InfluxScrapper.Models.Stock;

namespace InfluxScrapper.Utilities;

public static class HttpUtilities
{
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
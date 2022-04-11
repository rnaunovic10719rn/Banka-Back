using System.Text.Json.Serialization;

namespace InfluxScrapper.Models.Forex;

public class ForexExchangeRateJson
{
    [JsonPropertyName("Realtime Currency Exchange Rate")]
    public ForexExchangeRateJsonInner Body {get;set;}
}

public class ForexExchangeRateJsonInner
{
    [JsonPropertyName("5. Exchange Rate")]
    public double ExchangeRate { get; set;}
    
    [JsonPropertyName("8. Bid Price")]
    public double Bid { get; set;}
    
    [JsonPropertyName("9. Ask Price")]
    public double Ask { get; set;}
}
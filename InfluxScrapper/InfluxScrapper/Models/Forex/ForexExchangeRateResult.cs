using System.Text.Json.Serialization;
using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;

namespace InfluxScrapper.Models.Stock;

public class ForexExchangeRateResult
{
    [Index(0)]

    [Column("from", IsTag = true)]
    public string? FromCurrency { get; set; }
    
    [Index(0)]

    [Column("to", IsTag = true)]
    public string? ToCurrency { get; set; }
    
    [Ignore]
    [Column(IsTimestamp = true)] 
    public DateTime Time { get; set; } =  DateTime.SpecifyKind(DateTime.Now, DateTimeKind.Utc);
    
    [JsonPropertyName("5. Exchange Rate")]
    [Column("exchangeRate")]
    public double ExchangeRate { get; set;}
    
    [JsonPropertyName("8. Bid Price")]
    [Column("bid")]
    public double Bid { get; set;}
    
    [JsonPropertyName("9. Ask Price")]
    [Column("ask")]
    public double Ask { get; set;}

    public PointData ToPointData(string measurement)
        => PointData.Measurement(measurement)
            .Tag("from", FromCurrency)
            .Tag("to", ToCurrency)
            .Field("exchangeRate", ExchangeRate)
            .Field("bid", Bid)
            .Field("ask", Ask)
            .Timestamp(Time, WritePrecision.S);

    public static ForexExchangeRateResult FromRecord(FluxRecord record)
    {
        var stock = new ForexExchangeRateResult();
        stock.FromCurrency = record.Values["from"].ToString();
        stock.ToCurrency = record.Values["to"].ToString();
        stock.ExchangeRate = double.Parse(record.Values["exchangeRate"].ToString());
        stock.Bid = double.Parse(record.Values["bid"].ToString());
        stock.Ask = double.Parse(record.Values["ask"].ToString());
        if(record.GetTime() is not null)
            stock.Time = record.GetTime()!.Value.ToDateTimeUtc();
        return stock;
    }
}
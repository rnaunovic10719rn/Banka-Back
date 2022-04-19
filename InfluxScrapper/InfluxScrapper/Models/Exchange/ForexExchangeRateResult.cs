using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Influx;
using InfluxScrapper.Utilites;

namespace InfluxScrapper.Models.Exchange;

public class ForexExchangeRateResult : InvfluxRecord<ForexExchangeRateResult>
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
    public DateTime TimeWritten { get; set; } = DateTime.SpecifyKind(DateTime.Now, DateTimeKind.Utc);
    
    [Column("exchangeRate")]
    public double ExchangeRate { get; set;}
    
    [Column("bid")]
    public double Bid { get; set;}
    
    [Column("ask")]
    public double Ask { get; set;}

    public static PointData ToPointData(ForexExchangeRateResult item, string measurement)
        => PointData.Measurement(measurement)
            .Tag("from", item.FromCurrency)
            .Tag("to", item.ToCurrency)
            .Field("exchangeRate", item.ExchangeRate)
            .Field("bid", item.Bid)
            .Field("ask", item.Ask)
            .Field("written", item.TimeWritten.ToUnixTimestamp())
            .Timestamp(item.Time, WritePrecision.S);


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
        stock.TimeWritten = long.Parse(record.Values["written"].ToString()).ToDateTime();
        return stock;
    }
}
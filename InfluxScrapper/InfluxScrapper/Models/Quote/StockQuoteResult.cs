using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Influx;
using InfluxScrapper.Utilities;

namespace InfluxScrapper.Models.Quote;

public class StockQuoteResult : InvfluxRecord<StockQuoteResult>
{
    [Ignore]

    [Column("ticker", IsTag = true)]
    public string? Ticker { get; set; }
    
    [Ignore]
    [Column(IsTimestamp = true)] 
    public DateTime Time { get; set; } =  DateTime.SpecifyKind(DateTime.Now, DateTimeKind.Utc);
    
    [Ignore]
    public DateTime TimeWritten { get; set; } = DateTime.SpecifyKind(DateTime.Now, DateTimeKind.Utc);
    
    
    [Index(1)]
    [Column("open")]
    public double Open { get; set;}
    
    [Index(2)]
    [Column("high")]
    public double High { get; set;}
    
    [Index(3)]
    [Column("low")]
    public double Low { get; set;}
    
    [Index(4)]
    [Column("price")]
    public double Price { get; set;}
    
    [Index(5)]
    [Column("volume")]
    public long Volume { get; set;}
    
    [Index(7)]
    [Column("previousClose")]
    public double PreviousClose { get; set;}
    
    [Index(8)]
    [Column("change")]
    public double Change { get; set;}

    [Index(9)] [Column("changePercent")] 
    public string ChangePercentStr { get; set; } = "";

    [Ignore] public double ChangePercent => double.Parse(ChangePercentStr.Replace("%", ""));

    public static PointData ToPointData(StockQuoteResult item, string measurement)
        => PointData.Measurement(measurement)
            .Tag("ticker", item.Ticker)
            .Field("open", item.Open)
            .Field("low", item.Low)
            .Field("high", item.High)
            .Field("volume", item.Volume)
            .Field("price", item.Price)
            .Field("previousClose", item.PreviousClose)
            .Field("change", item.Change)
            .Field("changePercent", item.ChangePercent)
            .Field("written", item.TimeWritten.ToUnixTimestamp())
            .Timestamp(item.Time, WritePrecision.Ns);

    public static StockQuoteResult FromRecord(FluxRecord record)
    {
        var stock = new StockQuoteResult();
        stock.Ticker = record.Values["ticker"].ToString();
        stock.Open = double.Parse(record.Values["open"].ToString()!);
        stock.High = double.Parse(record.Values["high"].ToString()!);
        stock.Low = double.Parse(record.Values["low"].ToString()!);
        stock.Volume = long.Parse(record.Values["volume"].ToString()!);
        stock.Price = double.Parse(record.Values["price"].ToString()!);
        stock.PreviousClose = double.Parse(record.Values["previousClose"].ToString()!);
        stock.Change = double.Parse(record.Values["change"].ToString()!);
        stock.ChangePercentStr = record.Values["changePercent"].ToString()! + "%";
        if(record.GetTime() is not null)
            stock.Time = record.GetTime()!.Value.ToDateTimeUtc();
        stock.TimeWritten = long.Parse(record.Values["written"].ToString()!).ToDateTime();
        return stock;
    }
}
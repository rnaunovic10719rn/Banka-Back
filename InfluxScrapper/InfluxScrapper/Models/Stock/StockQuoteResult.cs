using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;

namespace InfluxScrapper.Models.Stock;

public class StockQuoteResult
{
    [Ignore]
    [Column("ticker", IsTag = true)]
    public string? Ticker { get; set; }
    
    [Column(IsTimestamp = true)] 
    public DateTime Time { get; set; } =  DateTime.SpecifyKind(DateTime.Now, DateTimeKind.Utc);
    
    [Index(0)]
    [Column("open")]
    public double Open { get; set;}
    
    [Index(1)]
    [Column("high")]
    public double High { get; set;}
    
    [Index(2)]
    [Column("low")]
    public double Low { get; set;}
    
    [Index(3)]
    [Column("price")]
    public double Price { get; set;}
    
    [Index(4)]
    [Column("volume")]
    public long Volume { get; set;}
    
    [Index(5)]
    [Column("previousClose")]
    public double PreviousClose { get; set;}
    
    [Index(6)]
    [Column("change")]
    public double Change { get; set;}
    
    [Index(7)]
    [Column("changePercent")]
    public double ChangePercent { get; set;}

    public PointData ToPointData(string measurement)
        => PointData.Measurement(measurement)
            .Tag("ticker", Ticker)
            .Field("open", Open)
            .Field("low", Low)
            .Field("high", High)
            .Field("volume", Volume)
            .Field("price", Price)
            .Field("previousClose", PreviousClose)
            .Field("change", Change)
            .Field("changePercent", ChangePercent)
            .Timestamp(Time, WritePrecision.Ns);

    public static StockQuoteResult FromRecord(FluxRecord record)
    {
        var stock = new StockQuoteResult();
        stock.Ticker = record.Values["ticker"].ToString();
        stock.Open = double.Parse(record.Values["open"].ToString());
        stock.High = double.Parse(record.Values["high"].ToString());
        stock.Low = double.Parse(record.Values["low"].ToString());
        stock.Volume = long.Parse(record.Values["volume"].ToString());
        stock.Price = double.Parse(record.Values["price"].ToString());
        stock.PreviousClose = double.Parse(record.Values["previousClose"].ToString());
        stock.Change = double.Parse(record.Values["change"].ToString());
        stock.ChangePercent = double.Parse(record.Values["changePercent"].ToString());
        if(record.GetTime() is not null)
            stock.Time = record.GetTime()!.Value.ToDateTimeUtc();
        return stock;
    }
}
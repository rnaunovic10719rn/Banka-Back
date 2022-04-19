using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Influx;
using InfluxScrapper.Utilites;

namespace InfluxScrapper.Models.Stock;


public class StockResult : InvfluxRecord<StockResult>
{
    [Ignore]
    [Column("ticker", IsTag = true)]
    public string? Ticker { get; set; }
    
    [Index(0)]
    public string Date { get; set;}

    [Ignore]
    [Column(IsTimestamp = true)]
    public DateTime Time
    {
        get => DateTime.SpecifyKind(DateTime.Parse(Date), DateTimeKind.Utc);
        set => Date = DateTime.SpecifyKind(value, DateTimeKind.Utc).ToString("o");
    } 
    
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
    [Column("close")]
    public double Close { get; set;}
    
    [Index(5)]
    [Column("volume")]
    public long Volume { get; set;}

    public static PointData ToPointData(StockResult item, string measurement)
        => PointData.Measurement(measurement)
            .Tag("ticker", item.Ticker)
            .Field("open", item.Open)
            .Field("close", item.Close)
            .Field("low", item.Low)
            .Field("high", item.High)
            .Field("volume", item.Volume)
            .Field("written", item.TimeWritten.ToUnixTimestamp())
            .Timestamp(item.Time, WritePrecision.Ns);

    public static StockResult FromRecord(FluxRecord record)
    {
        var stock = new StockResult();
        stock.Ticker = record.Values["ticker"].ToString();
        stock.Close = double.Parse(record.Values["close"].ToString());
        stock.Open = double.Parse(record.Values["open"].ToString());
        stock.High = double.Parse(record.Values["high"].ToString());
        stock.Low = double.Parse(record.Values["low"].ToString());
        stock.Volume = long.Parse(record.Values["volume"].ToString());
        stock.Date = record.GetTime().ToString();
        stock.TimeWritten = long.Parse(record.Values["written"].ToString()).ToDateTime();
        return stock;
    }

}
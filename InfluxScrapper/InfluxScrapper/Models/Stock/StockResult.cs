using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;

namespace InfluxScrapper.Models.Stock;


public class StockResult
{
    [Ignore]
    [Column("ticker", IsTag = true)]
    public string? Ticker { get; set; }
    
    [Index(0)]
    public string Time { get; set;}

    [Column(IsTimestamp = true)] 
    public DateTime Date => DateTime.SpecifyKind(DateTime.Parse(Time), DateTimeKind.Utc);
    
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

    public PointData ToPointData(string measurement)
        => PointData.Measurement(measurement)
            .Tag("ticker", Ticker)
            .Field("open", Open)
            .Field("close", Close)
            .Field("low", Low)
            .Field("high", High)
            .Field("volume", Volume)
            .Timestamp(Date, WritePrecision.Ns);

    public static StockResult FromRecord(FluxRecord record)
    {
        var stock = new StockResult();
        stock.Ticker = record.Values["ticker"].ToString();
        stock.Close = double.Parse(record.Values["close"].ToString());
        stock.Open = double.Parse(record.Values["open"].ToString());
        stock.High = double.Parse(record.Values["high"].ToString());
        stock.Low = double.Parse(record.Values["low"].ToString());
        stock.Volume = long.Parse(record.Values["volume"].ToString());
        stock.Time = record.Values["_date"].ToString();
        return stock;
    }

}
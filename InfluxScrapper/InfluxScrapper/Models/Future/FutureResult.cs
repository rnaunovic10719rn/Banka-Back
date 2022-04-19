using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Influx;

namespace InfluxScrapper.Models.Stock;


public class FutureResult : InvfluxRecord<FutureResult>
{
    [Ignore]
    [Column("symbol", IsTag = true)]
    public string? Symbol { get; set; }
    
    [Index(0)]
    public string Date { get; set;}
    
    [Ignore]
    [Column(IsTimestamp = true)] 
    public DateTime Time
    {
        get => DateTime.SpecifyKind(DateTime.Parse(Date), DateTimeKind.Utc);
        set => Date = DateTime.SpecifyKind(value, DateTimeKind.Utc).ToString("o");
    }
    
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
    [Column("settle")]
    public double Settle { get; set;}
    
    [Index(5)]
    [Column("volume")]
    public double VolumeDouble { get; set;}
    
    [Index(6)]
    [Column("previous")]
    public double PreviousDouble { get; set;}

    [Ignore] 
    public long Volume => (long)VolumeDouble;
    
    [Ignore] 
    public long Previous => (long)VolumeDouble;
    
    public static PointData ToPointData(FutureResult item, string measurement)
        => PointData.Measurement(measurement)
            .Tag("symbol", item.Symbol)
            .Field("open", item.Open)
            .Field("low", item.Low)
            .Field("high", item.High)
            .Field("settle", item.Settle)
            .Field("volume", item.Volume)
            .Field("previous", item.Previous)
            .Timestamp(item.Time, WritePrecision.Ns);

    public static FutureResult FromRecord(FluxRecord record)
    {
        var future = new FutureResult();
        future.Symbol = record.Values["symbol"].ToString();
        future.Settle = double.Parse(record.Values["settle"].ToString());
        future.Open = double.Parse(record.Values["open"].ToString());
        future.High = double.Parse(record.Values["high"].ToString());
        future.Low = double.Parse(record.Values["low"].ToString());
        future.VolumeDouble = long.Parse(record.Values["volume"].ToString());
        future.PreviousDouble = long.Parse(record.Values["previous"].ToString());
        future.Date = record.GetTime().ToString();
        return future;
    }

}
using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;

namespace InfluxScrapper.Models.Stock;


public class FutureResult
{
    [Ignore]
    [Column("symbol", IsTag = true)]
    public string? Symbol { get; set; }
    
    [Index(0)]
    public string Time { get; set;}
    
    [Ignore]

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
    
    public PointData ToPointData(string measurement)
        => PointData.Measurement(measurement)
            .Tag("symbol", Symbol)
            .Field("open", Open)
            .Field("low", Low)
            .Field("high", High)
            .Field("settle", Settle)
            .Field("volume", Volume)
            .Field("previous", Previous)
            .Timestamp(Date, WritePrecision.Ns);

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
        future.Time = record.GetTime().ToString();
        return future;
    }

}
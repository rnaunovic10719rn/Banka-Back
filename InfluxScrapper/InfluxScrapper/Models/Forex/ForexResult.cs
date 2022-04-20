using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Influx;
using InfluxScrapper.Utilites;

namespace InfluxScrapper.Models.Forex;


public class ForexResult : InvfluxRecord<ForexResult>
{
    [Ignore]
    [Column("from", IsTag = true)]
    public string? SymbolFrom { get; set; }
    
    [Ignore]
    [Column("to", IsTag = true)]
    public string? SymbolTo { get; set; }

    [Index(0)] public string Date { get; set; } = "";
    
    [Ignore]
    [Column(IsTimestamp = true)] 
    public DateTime Time
    {
        get => DateTime.SpecifyKind(DateTime.Parse(Date), DateTimeKind.Utc);
        set => Date = DateTime.SpecifyKind(value, DateTimeKind.Utc).ToString("o");
    }
    
    [Ignore]
    public DateTime TimeWritten { get; set; } = DateTime.Now;
    
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

    public static PointData ToPointData(ForexResult item, string measurement)
        => PointData.Measurement(measurement)
            .Tag("from", item.SymbolFrom)
            .Tag("to", item.SymbolTo)
            .Field("open", item.Open)
            .Field("close", item.Close)
            .Field("low", item.Low)
            .Field("high", item.High)
            .Field("written", item.TimeWritten.ToUnixTimestamp())
            .Timestamp(item.Time, WritePrecision.Ns);

    public static ForexResult FromRecord(FluxRecord record)
    {
        var forex = new ForexResult();
        forex.SymbolFrom = record.Values["from"].ToString();
        forex.SymbolTo = record.Values["to"].ToString();
        forex.Close = double.Parse(record.Values["close"].ToString()!);
        forex.Open = double.Parse(record.Values["open"].ToString()!);
        forex.High = double.Parse(record.Values["high"].ToString()!);
        forex.Low = double.Parse(record.Values["low"].ToString()!);
        forex.Date = record.GetTime().ToString()!;
        forex.TimeWritten = long.Parse(record.Values["written"].ToString()!).ToDateTime();
        return forex;
    }

}
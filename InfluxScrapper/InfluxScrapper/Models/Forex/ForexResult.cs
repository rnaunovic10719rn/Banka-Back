using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;

namespace InfluxScrapper.Models.Stock;


public class ForexResult
{
    [Ignore]
    [Column("from", IsTag = true)]
    public string? SymbolFrom { get; set; }
    
    [Ignore]
    [Column("to", IsTag = true)]
    public string? SymbolTo { get; set; }
    
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
    [Column("close")]
    public double Close { get; set;}

    public PointData ToPointData(string measurement)
        => PointData.Measurement(measurement)
            .Tag("from", SymbolFrom)
            .Tag("to", SymbolTo)
            .Field("open", Open)
            .Field("close", Close)
            .Field("low", Low)
            .Field("high", High)
            .Timestamp(Date, WritePrecision.Ns);

    public static ForexResult FromRecord(FluxRecord record)
    {
        var forex = new ForexResult();
        forex.SymbolFrom = record.Values["from"].ToString();
        forex.SymbolTo = record.Values["to"].ToString();
        forex.Close = double.Parse(record.Values["close"].ToString());
        forex.Open = double.Parse(record.Values["open"].ToString());
        forex.High = double.Parse(record.Values["high"].ToString());
        forex.Low = double.Parse(record.Values["low"].ToString());
        forex.Time = record.GetTime().ToString();;
        return forex;
    }

}
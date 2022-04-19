using CsvHelper.Configuration.Attributes;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Stock;

namespace InfluxScrapper.Future;

public class FutureDataResult
{
    [Index(0)] public string Symbol { get; set; } = "";
    
    [Index(1)]
    public string Exhange { get; set; } = "";
    
    [Index(2)]
    public string Name { get; set; } = "";
    
    [Index(3)]
    public string Months { get; set; } = "";
    
    public PointData ToPointData(string measurement)
        => PointData.Measurement(measurement)
            .Tag("symbol", Symbol)
            .Field("exchange", Exhange)
            .Field("name", Name)
            .Field("months", Months)
            .Timestamp(DateTime.SpecifyKind(DateTime.Now, DateTimeKind.Utc), WritePrecision.Ns);

    public static FutureDataResult FromRecord(FluxRecord record)
    {
        var future = new FutureDataResult();
        future.Symbol = record.Values["symbol"].ToString()!;
        future.Exhange = record.Values["exchange"].ToString()!; 
        future.Name = record.Values["name"].ToString()!;
        future.Months = record.Values["months"].ToString()!;
        return future;
    }
}
using System;
using InfluxDB.Client.Api.Domain;
using InfluxDB.Client.Core.Flux.Domain;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Influx;
using InfluxScrapper.Utilites;

namespace InfluxScrapper.MockTests;

public class MockResult : InvfluxRecord<MockResult>
{
    public DateTime Time { get; set; } = DateTime.SpecifyKind(DateTime.Now, DateTimeKind.Utc);
    public DateTime TimeWritten { get; set; }
    public int Value { get; set; }
    public string Tag { get; set; } = "test";
    
    public static PointData ToPointData(MockResult item, string measurement)
        => PointData.Measurement(measurement)
            .Tag("tag", item.Tag)
            .Field("written", item.TimeWritten.ToUnixTimestamp())
            .Field("value", item.Value)
            .Timestamp(item.Time, WritePrecision.Ns);

    public static MockResult FromRecord(FluxRecord record)
    {
        var result = new MockResult();
        result.Tag = record.Values["tag"].ToString()!;
        result.Time = record.GetTime()!.Value.ToDateTimeUtc();
        result.TimeWritten = long.Parse(record.Values["written"].ToString()!).ToDateTime();
        result.Value = int.Parse(record.Values["value"].ToString()!); 
        
        return result;
    }
}
using System.Runtime.Versioning;
using InfluxScrapper.Models.Influx;
[assembly:RequiresPreviewFeatures]

namespace InfluxScrapper.MockTests;

public class MockUpdateQuery : IInfluxMeasurementHolder
{
    public MockUpdateQuery(int lenght)
    {
        Length = lenght;
    }
    public string Measurement => "test";
    
    public int Length { get; }

}
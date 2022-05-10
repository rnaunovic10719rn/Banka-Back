using System.Runtime.Versioning;
using InfluxScrapper.Models.Influx;

namespace InfluxScrapper.MockTests;

public class MockUpdateQuery : IInfluxMeasurementHolder
{
    public MockUpdateQuery(int lenght, int id)
    {
        Length = lenght;
        Id = id;
    }
    public string Measurement => "test";
    
    public int Length { get; }
    public int Id { get; }
}
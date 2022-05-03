using System;
using InfluxScrapper.Models.Influx;

namespace InfluxScrapper.MockTests;

public class MockReadQuery : InfluxCacheQuery<MockReadQuery>
{
    public MockReadQuery(int lenght)
    {
        Lenght = lenght;
    }
    public string Measurement => "test";
    public DateTime? TimeFrom { get; init; }
    public DateTime? TimeTo { get; init; }
    public int Lenght { get; }

    public string ToQuery(bool singleFile = false) => singleFile.ToString();
}
using System;
using InfluxScrapper.Models.Influx;

namespace InfluxScrapper.MockTests;

public class MockReadQuery : InfluxCacheQuery<MockReadQuery>
{
    public MockReadQuery(int lenght, int id)
    {
        Lenght = lenght;
        Id = id;
    }
    public string Measurement => "test";
    public DateTime? TimeFrom { get; init; }
    public DateTime? TimeTo { get; init; }
    public int Lenght { get; }
    public int Id {get;}

    public string ToQuery(bool singleFile = false) => singleFile.ToString();
}
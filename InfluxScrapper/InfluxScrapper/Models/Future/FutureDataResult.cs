using CsvHelper.Configuration.Attributes;

namespace InfluxScrapper.Future;

public class FutureDataResult
{
    [Index(0)]
    public string Symbol { get; set; }
    
    [Index(1)]
    public string Exhange { get; set; }
    
    [Index(2)]
    public string Name { get; set; }
    
    [Index(3)]
    public string Months { get; set; }
}
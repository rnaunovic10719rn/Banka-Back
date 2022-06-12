namespace InfluxScrapper;

public static class Constants
{
    public static readonly string InfluxOrg = "raf";
    public static readonly string InfluxToken = "mytoken";
    
    public static readonly string InfluxDBUrl = Environment.GetEnvironmentVariable("INFLUX_DB_URL") 
                                                ?? "http://db-influx:8086";
    
    public static readonly string InfluxBucket = "stocks";
    
    public static readonly string AlphaVantageApiKey = Environment.GetEnvironmentVariable("ALPHAVANTAGE_TOKEN") 
                                                       ?? "alpha-key";

    public static readonly string NasdaqApiKey = Environment.GetEnvironmentVariable("NASDAQ_API_KEY") 
                                                 ?? "nasdaq-key";
}
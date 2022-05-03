namespace InfluxScrapper.MockTests;

public class MockScrapeQuery
{
    public MockScrapeQuery(int id)
    {
        ID = id;
    }
    public int ID { get; set; }
}
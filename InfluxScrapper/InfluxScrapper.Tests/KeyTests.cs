using Xunit;

namespace InfluxScrapper.Tests;

public class KeyTests
{
    [Fact]
    public void TestAlphaVantageApiKeyNotDefault()
    {
        Assert.NotEqual("alpha-key",Constants.AlphaVantageApiKey);  
    }
    
    [Fact]
    public void TestNasdaqApiKeyNotDefault()
    {
        Assert.NotEqual("nasdaq-key",Constants.NasdaqApiKey);  
    }
}
using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.Text;

namespace InfluxScrapper.Models.Future;

public class FutureQuery
{
    
    
    [Required]
    [RegularExpression("^[a-zA-Z]{3,6}\\d{4}$")]
    [DefaultValue("FVSJ2022")]
    public string Symbol { get; set; }

    public string Url => $"https://data.nasdaq.com/api/v3/datasets/EUREX/FVSJ2022.csv?api_key={Constants.NasdaqApiKey}";
    
}
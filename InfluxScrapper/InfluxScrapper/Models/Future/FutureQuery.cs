using System.ComponentModel;
using System.ComponentModel.DataAnnotations;
using System.Text;
using InfluxScrapper.Models.Influx;

namespace InfluxScrapper.Models.Future;

public class FutureQuery : IInfluxMeasurementHolder
{


    [Required]
    [RegularExpression("^[a-zA-Z]{2,6}\\d{4}$")]
    [DefaultValue("FVSJ2022")]
    public string Symbol { get; set; } = "";

    public string Url => $"https://data.nasdaq.com/api/v3/datasets/EUREX/{Symbol}.csv?api_key={Constants.NasdaqApiKey}";

    public string Measurement => "futures";
}
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using InfluxDB.Client.Writes;
using InfluxScrapper.Models.Influx;

namespace InfluxScrapper.MockTests;

public class MockInfluxManager : IInfluxManager
{
    public List<PointData> Items { get; } = new();

    public Task<IEnumerable<T>> Query<T>(string query, CancellationToken token) where T : InvfluxRecord<T>
    {
        throw new System.NotImplementedException();
    }

    public async Task Upload<T>(IEnumerable<T> records, string measurement, CancellationToken token) where T : InvfluxRecord<T>
    {
        var points = records
            .Select(r=>T.ToPointData(r, measurement))
            .ToArray();
        Items.AddRange(points);
    }
}
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
    public Dictionary<PointData, object> Table = new();

    public Task<IEnumerable<T>> Query<T>(string query, CancellationToken token) where T : InvfluxRecord<T>
    {
        if (query == true.ToString())
        {
            if(Items.LastOrDefault() is {} last)
                return Task.FromResult<IEnumerable<T>>(new T[] {(T)Table[last]});
            return Task.FromResult(Enumerable.Empty<T>());
        }

        var results = Table.Values.Select(v => (T) v).ToArray();
        return Task.FromResult<IEnumerable<T>>(results);
    }

    public Task Upload<T>(IEnumerable<T> records, string measurement, CancellationToken token) where T : InvfluxRecord<T>
    {
        var recordsArr = records.ToArray();
        var points = new PointData[recordsArr.Length];
        for (var i = 0; i < recordsArr.Length; i++)
        {
            var point = T.ToPointData(recordsArr[i], measurement);
            Table[point] = recordsArr[i];
            points[i] = point;
        }
        Items.AddRange(points);
        return Task.CompletedTask;
    }
}
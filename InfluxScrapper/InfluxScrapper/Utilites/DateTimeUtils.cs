namespace InfluxScrapper.Utilites;

public static class DateTimeUtils
{
    public static long ToUnixTimestamp(this DateTime time)
    {
        var span = DateTime.SpecifyKind(time, DateTimeKind.Utc) - DateTime.SpecifyKind(DateTime.UnixEpoch, DateTimeKind.Utc);
        return (long)span.TotalSeconds;
    }

    public static DateTime ToDateTime(this long time)
    {
        var span = TimeSpan.FromSeconds(time);
        return DateTime.SpecifyKind(DateTime.UnixEpoch.Add(span), DateTimeKind.Utc);
    }
}
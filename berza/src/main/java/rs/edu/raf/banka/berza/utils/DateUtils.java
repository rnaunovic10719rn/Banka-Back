package rs.edu.raf.banka.berza.utils;

import java.time.ZonedDateTime;
import java.util.Date;

public class DateUtils {

    public static boolean isDateInDecayDays(Date date, Integer days) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime daysAgo = now.plusDays(-1L * days);

        return date.toInstant().isBefore(daysAgo.toInstant());
    }

    private DateUtils() {

    }

}

package rs.edu.raf.banka.berza.utils;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

public class DateUtils {

    public static boolean isDateInDecayDays(Date date, Integer days) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime daysAgo = now.plusDays(-1L * days);

        return date.toInstant().isBefore(daysAgo.toInstant());
    }

    public static class StartEndDateTime {
        public String startDate;
        public String endDate;

        public StartEndDateTime(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }

    public static StartEndDateTime getStartEndDateTime(String type, String interval, String reqType) {
        return getStartEndDateTime(type, interval, reqType, null, null);
    }

    public static StartEndDateTime getStartEndDateTime(String type, String interval, String reqType, ZoneId zoneId, String openHour) {
        DateTimeFormatter startFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateTimeFormatter endFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        ZonedDateTime zonedDateTime = DateUtils.getZonedDateTime(zoneId);
        String endDate = zonedDateTime.format(endFormatter);

        if(openHour != null && openHour.length() > 0) {
            openHour = openHour.trim();
        }

        if(type.equals("intraday") && interval.equals("5min")) {
            switch (zonedDateTime.getDayOfWeek()) {
                case SATURDAY:
                case SUNDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
                    break;
                case MONDAY:
                    if(openHour != null && (zonedDateTime.getHour() < getHourFromString(openHour) || (zonedDateTime.getHour() == getHourFromString(openHour) && zonedDateTime.getMinute() < getMinuteFromString(openHour)))) {
                        zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
                    } else {
                        zonedDateTime = zonedDateTime.minusDays(1);
                    }
                default:
                    zonedDateTime = zonedDateTime.minusDays(1);
            }
        } else if(type.equals("intraday") && interval.equals("30min")) {
            switch (zonedDateTime.getDayOfWeek()) {
                case SATURDAY:
                case SUNDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    break;
                case MONDAY:
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
                    break;
                default:
                    zonedDateTime = zonedDateTime.minusDays(7); // 7 zbog vikenda
            }
        } else {
            switch (reqType) {
                case "1m":
                    zonedDateTime = zonedDateTime.minusMonths(1);
                    break;
                case "6m":
                    zonedDateTime = zonedDateTime.minusMonths(6);
                    break;
                case "1y":
                    zonedDateTime = zonedDateTime.minusMonths(12);
                    break;
                case "2y":
                    zonedDateTime = zonedDateTime.minusMonths(24);
                    break;
                case "ytd":
                    zonedDateTime = zonedDateTime.with(firstDayOfYear());
                    break;
            }
        }

        String startDate = zonedDateTime.format(startFormatter);

        return new StartEndDateTime(startDate, endDate);
    }

    public static Integer getHourFromString(String t) {
        if(!t.contains(":")) {
            return -1;
        }
        return Integer.parseInt(t.split(":")[0]);
    }

    public static Integer getMinuteFromString(String t) {
        if(!t.contains(":")) {
            return -1;
        }
        return Integer.parseInt(t.split(":")[1]);
    }

    public static ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.now();
    }

    public static ZonedDateTime getZonedDateTime(ZoneId zoneId) {
        if(zoneId == null) {
            return getZonedDateTime();
        }
        return ZonedDateTime.now(zoneId);
    }

    private DateUtils() {

    }

}

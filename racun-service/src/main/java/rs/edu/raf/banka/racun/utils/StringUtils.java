package rs.edu.raf.banka.racun.utils;

public class StringUtils {

    private StringUtils() {

    }

    public static boolean emptyString(String s) {
        return s == null || s.isBlank();
    }
}

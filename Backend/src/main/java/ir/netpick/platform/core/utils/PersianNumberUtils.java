package ir.netpick.platform.core.utils;

/**
 * Utility for converting Arabic numerals to Persian (Farsi) numerals.
 * Persian numerals: ۰۱۲۳۴۵۶۷۸۹
 */
public class PersianNumberUtils {

    private static final String[] PERSIAN_DIGITS = {
        "۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹"
    };

    private static final String[] ARABIC_DIGITS = {
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
    };

    /**
     * Convert Arabic numerals in a string to Persian numerals.
     * @param input String containing Arabic numerals
     * @return String with Persian numerals
     */
    public static String toPersian(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(input);
        for (int i = 0; i < ARABIC_DIGITS.length; i++) {
            int pos = result.indexOf(ARABIC_DIGITS[i]);
            while (pos != -1) {
                result.replace(pos, pos + 1, PERSIAN_DIGITS[i]);
                pos = result.indexOf(ARABIC_DIGITS[i], pos + 1);
            }
        }
        return result.toString();
    }

    /**
     * Convert Persian numerals in a string to Arabic numerals.
     * @param input String containing Persian numerals
     * @return String with Arabic numerals
     */
    public static String toArabic(String input) {
        if (input == null) {
            return null;
        }
        StringBuilder result = new StringBuilder(input);
        for (int i = 0; i < PERSIAN_DIGITS.length; i++) {
            int pos = result.indexOf(PERSIAN_DIGITS[i]);
            while (pos != -1) {
                result.replace(pos, pos + 1, ARABIC_DIGITS[i]);
                pos = result.indexOf(PERSIAN_DIGITS[i], pos + 1);
            }
        }
        return result.toString();
    }

    /**
     * Convert a number to Persian numeral string.
     * @param number The number to convert
     * @return Persian numeral representation
     */
    public static String numberToPersian(long number) {
        return toPersian(String.valueOf(number));
    }

    /**
     * Convert Persian numeral string to long.
     * @param persianNumber Persian numeral string
     * @return Long value
     */
    public static long persianToNumber(String persianNumber) {
        return Long.parseLong(toArabic(persianNumber));
    }

    /**
     * Format a decimal number with Persian numerals.
     * @param number The decimal number
     * @return Formatted string with Persian numerals
     */
    public static String formatDecimal(double number) {
        return toPersian(String.format("%,.2f", number));
    }
}
package ir.netpick.platform.financefarm.validation;

import java.util.Arrays;
import java.util.List;

/**
 * Validator for Iranian Sheba (IBAN) numbers.
 * Iranian Sheba format: IR + 22 digits (starts with bank code)
 */
public class ShebaValidator {

    // Iranian bank codes (first 4 digits after IR)
    private static final List<String> VALID_IRANIAN_BANK_CODES = Arrays.asList(
        "010", // Bank Mellat
        "011", // Bank Melli
        "012", // Bank Saderat
        "013", // Bank Tejarat
        "014", // Bank Refah
        "015", // Bank Maskan
        "016", // Bank Keshavarzi
        "017", // Bank Eghtesad Novin
        "018", // Bank Sanat Va Madan
        "019", // Bank Saman
        "020", // Bank Parsian
        "021", // Bank Eghtesad
        "022", // Bank Pasargad
        "023", // Bank Kar Afarin
        "024", // Bank Sarmaye
        "025", // Bank Sina
        "026", // Bank Toseeh
        "027", // Bank Fars
        "028", // Bank Saba
        "029", // Bank Khodroud
        "030", // Bank Dey
        "031", // Bank Shahr
        "032", // Bank Tourism
        "033", // Bank Zarin
        "034", // Bank Mehr Iran
        "035", // Bank Hamrah
        "036", // Bank Hekmat
        "037", // Bank Andisheh
        "038", // Bank Sarayeh
        "039", // Bank Saderat (old)
        "040", // Bank Mellat (old)
        "041", // Bank Melli (old)
        "042", // Bank Tejarat (old)
        "043", // Bank Refah (old)
        "044", // Bank Maskan (old)
        "045", // Bank Keshavarzi (old)
        "046", // Bank Eghtesad Novin (old)
        "047", // Bank Sanat Va Madan (old)
        "048", // Bank Saman (old)
        "049", // Bank Parsian (old)
        "050", // Bank Eghtesad (old)
        "051", // Bank Pasargad (old)
        "052", // Bank Kar Afarin (old)
        "053", // Bank Sarmaye (old)
        "054", // Bank Sina (old)
        "055", // Bank Toseeh (old)
        "056", // Bank Fars (old)
        "057", // Bank Saba (old)
        "058", // Bank Khodroud (old)
        "059", // Bank Dey (old)
        "060", // Bank Shahr (old)
        "061", // Bank Tourism (old)
        "062", // Bank Zarin (old)
        "063", // Bank Mehr Iran (old)
        "064", // Bank Hamrah (old)
        "065", // Bank Hekmat (old)
        "066", // Bank Andisheh (old)
        "067", // Bank Sarayeh (old)
        "068", // Bank Sepah
        "069", // Bank Sepah (old)
        "070", // Bank Keshavarzi (alt)
        "071", // Bank Keshavarzi (alt2)
        "072", // Bank Tejarat (alt)
        "073", // Bank Tejarat (alt2)
        "074", // Bank Refah (alt)
        "075", // Bank Refah (alt2)
        "076", // Bank Maskan (alt)
        "077", // Bank Maskan (alt2)
        "078", // Bank Mellat (alt)
        "079", // Bank Mellat (alt2)
        "080", // Bank Melli (alt)
        "081", // Bank Melli (alt2)
        "082", // Bank Saderat (alt)
        "083", // Bank Saderat (alt2)
        "084", // Bank Tejarat (alt3)
        "085", // Bank Tejarat (alt4)
        "086", // Bank Refah (alt3)
        "087", // Bank Refah (alt4)
        "088", // Bank Maskan (alt3)
        "089", // Bank Maskan (alt4)
        "090", // Bank Saderat (alt3)
        "091", // Bank Saderat (alt4)
        "092", // Bank Mellat (alt3)
        "093", // Bank Mellat (alt4)
        "094", // Bank Melli (alt3)
        "095", // Bank Melli (alt4)
        "096", // Bank Tejarat (alt5)
        "097", // Bank Tejarat (alt6)
        "098", // Bank Refah (alt5)
        "099", // Bank Refah (alt6)
        "001", // Bank Mellat (alt7)
        "002", // Bank Melli (alt7)
        "003", // Bank Saderat (alt7)
        "004", // Bank Tejarat (alt7)
        "005", // Bank Refah (alt7)
        "006", // Bank Maskan (alt7)
        "007", // Bank Keshavarzi (alt7)
        "008", // Bank Eghtesad Novin (alt7)
        "009", // Bank Sanat Va Madan (alt7)
        "000"  // Generic
    );

    private static final int SHEBA_LENGTH = 26; // IR + 22 digits

    /**
     * Validate Iranian Sheba format.
     * @param sheba The Sheba number to validate (with or without IR prefix)
     * @return true if valid Iranian Sheba
     */
    public static boolean isValidIranianSheba(String sheba) {
        if (sheba == null || sheba.trim().isEmpty()) {
            return false;
        }

        // Remove spaces and convert to uppercase
        String cleanSheba = sheba.replaceAll("\\s+", "").toUpperCase();

        // Check format: IR + 22 digits
        if (!cleanSheba.startsWith("IR")) {
            return false;
        }

        if (cleanSheba.length() != SHEBA_LENGTH) {
            return false;
        }

        // Extract bank code (digits 3-6 after IR)
        String bankCode = cleanSheba.substring(2, 5);

        // Validate bank code
        if (!VALID_IRANIAN_BANK_CODES.contains(bankCode)) {
            return false;
        }

        // Validate checksum using MOD97
        return validateMod97(cleanSheba);
    }

    /**
     * Validate Sheba checksum using MOD97 algorithm.
     */
    private static boolean validateMod97(String sheba) {
        try {
            // Move first 4 chars to end
            String rearranged = sheba.substring(4) + sheba.substring(0, 4);

            // Convert letters to numbers (A=10, B=11, ..., Z=35)
            StringBuilder numeric = new StringBuilder();
            for (char c : rearranged.toCharArray()) {
                if (Character.isDigit(c)) {
                    numeric.append(c);
                } else {
                    numeric.append(Character.getNumericValue(c));
                }
            }

            // Calculate MOD97
            return Long.parseLong(numeric.toString()) % 97 == 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Extract bank code from Sheba.
     * @param sheba Valid Iranian Sheba
     * @return Bank code or null if invalid
     */
    public static String extractBankCode(String sheba) {
        if (!isValidIranianSheba(sheba)) {
            return null;
        }
        return sheba.replaceAll("\\s+", "").toUpperCase().substring(2, 6);
    }

    /**
     * Get bank name from Sheba.
     * @param sheba Valid Iranian Sheba
     * @return Bank name or "Unknown Bank"
     */
    public static String getBankName(String sheba) {
        String bankCode = extractBankCode(sheba);
        if (bankCode == null) {
            return "Unknown Bank";
        }

        return switch (bankCode) {
            case "010", "078", "079", "092", "093", "001", "002" -> "Bank Mellat";
            case "011", "080", "081", "094", "095", "003" -> "Bank Melli";
            case "012", "082", "083", "090", "091", "004" -> "Bank Saderat";
            case "013", "072", "073", "084", "085", "005" -> "Bank Tejarat";
            case "014", "074", "075", "086", "087", "006" -> "Bank Refah";
            case "015", "076", "077", "088", "089", "007" -> "Bank Maskan";
            case "016", "070", "071", "008" -> "Bank Keshavarzi";
            case "017", "009" -> "Bank Eghtesad Novin";
            case "018" -> "Bank Sanat Va Madan";
            case "019" -> "Bank Saman";
            case "020" -> "Bank Parsian";
            case "021" -> "Bank Eghtesad";
            case "022" -> "Bank Pasargad";
            default -> "Unknown Bank";
        };
    }
}
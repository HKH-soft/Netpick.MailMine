package ir.netpick.platform.ai.util;

public final class AiUtils {

    private AiUtils() {}

    public static String sanitizeForPrompt(String text) {
        if (text == null) return "";
        String result = text;
        result = result.replace("````", "");
        String[] lines = result.split("\n");
        StringBuilder cleaned = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.strip();
            if (trimmed.isEmpty()) continue;
            boolean isJsonLike = false;
            if (trimmed.startsWith("\"")) {
                isJsonLike = trimmed.contains("\":");
            } else if (trimmed.contains(":")) {
                String key = trimmed.substring(0, trimmed.indexOf(":")).strip();
                isJsonLike = !key.isEmpty() && key.chars().allMatch(Character::isLetterOrDigit);
            }
            if (!isJsonLike) {
                cleaned.append(line).append("\n");
            }
        }
        result = cleaned.toString();
        result = result.replaceAll("(?i)(ignore|disregard|forget|system|assistant|previous|instructions?)", "");
        return result.trim();
    }

    public static String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "";

        start += search.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;

        if (start >= json.length()) return "";

        char quote = json.charAt(start);
        if (quote == '"') {
            start++;
            int end = json.indexOf('"', start);
            return end > start ? json.substring(start, end) : "";
        } else {
            int end = start;
            while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
            return json.substring(start, end).trim();
        }
    }

    public static boolean isValidEmailFormat(String email) {
        return email != null && email.contains("@");
    }
}
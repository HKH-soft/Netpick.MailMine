package ir.netpick.mailmine.scrape.parser;

import ir.netpick.mailmine.scrape.model.Contact;
import org.apache.commons.validator.routines.EmailValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactInfoParser {

    private static final Logger log = LoggerFactory.getLogger(ContactInfoParser.class);
    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b",
            Pattern.CASE_INSENSITIVE);

    public static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}" +
                    "|\\+?[0-9][0-9\\s().\\-]{6,}[0-9]");

    public static final Pattern LINKEDIN_PATTERN = Pattern.compile(
            "(?i)https?://(?:www\\.)?linkedin\\.com/(?:in|pub)/[a-zA-Z0-9\\-_]+/?");

    public static final Pattern TWITTER_PATTERN = Pattern.compile(
            "(?i)https?://(?:www\\.)?(?:x|twitter)\\.com/([a-zA-Z0-9_]{1,15})");

    public static final Pattern GITHUB_PATTERN = Pattern.compile(
            "(?i)https?://(?:www\\.)?github\\.com/([a-zA-Z0-9\\-_]+)");

    public static final Pattern NAME_PATTERN = Pattern.compile(
            "\\b([A-Z][a-z]+(?:\\s[A-Z][a-z]+){1,3})\\b",
            Pattern.CASE_INSENSITIVE);


    public static Contact parse(String htmlContent) {
        Contact contact = new Contact();
        if (htmlContent == null || htmlContent.isBlank()) {
            log.debug("Empty or null HTML input provided.");
            return contact;
        }

        Document doc = Jsoup.parse(htmlContent);
        String cleanText = doc.text().trim(); // Direct text extraction, no re-clean

        extractFromText(cleanText, contact);
        extractFromLinks(doc, contact);

        normalizeAndValidate(contact);

        log.debug("Extracted contact info: emails={}, phones={}, linkedIn={}, twitter={}, github={}, names={}",
                contact.getEmails().size(), contact.getPhoneNumbers().size(), contact.getLinkedInUrls().size(),
                contact.getTwitterHandles().size(), contact.getGithubProfiles().size(), contact.getNames().size());

        return contact;
    }

    private static void extractFromText(String text, Contact contact) {
        if (text.isEmpty())
            return;

        extract(text, EMAIL_PATTERN, contact.getEmails());
        extract(text, PHONE_PATTERN, contact.getPhoneNumbers());
        extract(text, LINKEDIN_PATTERN, contact.getLinkedInUrls());
        extract(text, TWITTER_PATTERN, contact.getTwitterHandles());
        extract(text, GITHUB_PATTERN, contact.getGithubProfiles());
        extract(text, NAME_PATTERN, contact.getNames());
    }

    private static void extractFromLinks(Document doc, Contact contact) {
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String href = link.attr("abs:href").trim();
            if (href.isBlank())
                continue;

            try {
                if (href.startsWith("mailto:")) {
                    String email = href.substring(7).split("\\?")[0].trim();
                    if (EMAIL_VALIDATOR.isValid(email)) {
                        contact.getEmails().add(email);
                    } else {
                        log.debug("Invalid email from mailto: {}", email);
                    }
                } else if (LINKEDIN_PATTERN.matcher(href).matches()) {
                    addUrlDeduplicated(contact.getLinkedInUrls(), href);
                } else if (TWITTER_PATTERN.matcher(href).matches()) {
                    Matcher m = TWITTER_PATTERN.matcher(href);
                    if (m.find()) {
                        contact.getTwitterHandles().add(m.group(1));
                    }
                } else if (GITHUB_PATTERN.matcher(href).matches()) {
                    addUrlDeduplicated(contact.getGithubProfiles(), href);
                }
            } catch (Exception e) {
                log.warn("Error processing link: {}", href, e);
            }
        }
    }

    private static void normalizeAndValidate(Contact contact) {
        // Validate emails (remove invalid)
        contact.getEmails().removeIf(email -> !EMAIL_VALIDATOR.isValid(email));

        // Normalize phones
        Set<String> normalizedPhones = new java.util.HashSet<>();
        for (String phone : contact.getPhoneNumbers()) {
            String normalized = normalizePhoneNumber(phone);
            if (normalized != null && !normalized.isEmpty()) { // Add phone validator here if available
                normalizedPhones.add(normalized);
            }
        }
        contact.setPhoneNumbers(normalizedPhones);

        // Optional: Normalize names/URLs further (e.g., URL existence check async if
        // needed)
    }

    private static void extract(String text, Pattern pattern, Set<String> target) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String val = matcher.groupCount() >= 1 ? matcher.group(1) : matcher.group();
            if (val != null && !val.trim().isEmpty()) {
                String candidate = val.trim();
                if (pattern == LINKEDIN_PATTERN || pattern == GITHUB_PATTERN) {
                    addUrlDeduplicated(target, candidate);
                } else {
                    target.add(candidate);
                }
            }
        }
    }

    private static String normalizePhoneNumber(String phone) {
        if (phone == null)
            return null;
        String stripped = phone.replaceAll("[^+\\d]", "");
        return stripped.isEmpty() ? null : stripped;
    }

    private static void addUrlDeduplicated(Set<String> urlSet, String url) {
        if (url.isBlank())
            return;
        try {
            URI uri = URI.create(url);
            URL parsed = uri.toURL();
            String normalized = "https" + parsed.getHost().toLowerCase() + parsed.getFile();
            urlSet.add(normalized);
        } catch (MalformedURLException e) {
            log.debug("Invalid URL skipped: {}", url);
        }
    }
}
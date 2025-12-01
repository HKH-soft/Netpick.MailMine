package ir.netpick.mailmine.scrape.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ir.netpick.mailmine.scrape.model.Contact;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ContactInfoParser {

    private static final EmailValidator EMAIL_VALIDATOR = EmailValidator.getInstance();

    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b",
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

        log.debug("Extracted contact info: emails={}",
                contact.getEmails().size());

        return contact;
    }

    private static void extractFromText(String text, Contact contact) {
        if (text.isEmpty())
            return;

        extract(text, EMAIL_PATTERN, contact.getEmails());
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
                }
            } catch (Exception e) {
                log.warn("Error processing link: {}", href, e);
            }
        }
    }

    private static void normalizeAndValidate(Contact contact) {
        // Validate emails (remove invalid)
        contact.getEmails().removeIf(email -> !EMAIL_VALIDATOR.isValid(email));
    }

    private static void extract(String text, Pattern pattern, Set<String> target) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String val = matcher.groupCount() >= 1 ? matcher.group(1) : matcher.group();
            if (val != null && !val.trim().isEmpty()) {
                String candidate = val.trim();
                target.add(candidate);
            }
        }
    }
}
package ir.netpick.mailmine.scrape.parser;

import ir.netpick.mailmine.scrape.model.Contact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContactInfoParser Tests")
class ContactInfoParserTest {

    @Nested
    @DisplayName("parse() method")
    class ParseMethod {

        @Test
        @DisplayName("should return empty contact when HTML is null")
        void shouldReturnEmptyContactWhenHtmlIsNull() {
            Contact contact = ContactInfoParser.parse(null);

            assertNotNull(contact);
            assertTrue(contact.getEmails().isEmpty());
            assertFalse(contact.hasContactInfo());
        }

        @Test
        @DisplayName("should return empty contact when HTML is empty")
        void shouldReturnEmptyContactWhenHtmlIsEmpty() {
            Contact contact = ContactInfoParser.parse("");

            assertNotNull(contact);
            assertTrue(contact.getEmails().isEmpty());
            assertFalse(contact.hasContactInfo());
        }

        @Test
        @DisplayName("should return empty contact when HTML is blank")
        void shouldReturnEmptyContactWhenHtmlIsBlank() {
            Contact contact = ContactInfoParser.parse("   \n\t  ");

            assertNotNull(contact);
            assertTrue(contact.getEmails().isEmpty());
            assertFalse(contact.hasContactInfo());
        }

        @Test
        @DisplayName("should extract email from plain text in HTML")
        void shouldExtractEmailFromPlainText() {
            String html = "<html><body><p>Contact us at info@example.com for more details.</p></body></html>";

            Contact contact = ContactInfoParser.parse(html);

            assertNotNull(contact);
            assertTrue(contact.hasContactInfo());
            assertEquals(1, contact.getEmails().size());
            assertTrue(contact.getEmails().contains("info@example.com"));
        }

        @Test
        @DisplayName("should extract multiple emails from HTML")
        void shouldExtractMultipleEmails() {
            String html = """
                    <html><body>
                        <p>Contact: john@example.com</p>
                        <p>Support: support@company.org</p>
                        <p>Sales: sales@business.net</p>
                    </body></html>
                    """;

            Contact contact = ContactInfoParser.parse(html);

            assertNotNull(contact);
            assertTrue(contact.hasContactInfo());
            assertEquals(3, contact.getEmails().size());
            assertTrue(contact.getEmails().contains("john@example.com"));
            assertTrue(contact.getEmails().contains("support@company.org"));
            assertTrue(contact.getEmails().contains("sales@business.net"));
        }

        @Test
        @DisplayName("should extract email from mailto links")
        void shouldExtractEmailFromMailtoLinks() {
            String html = """
                    <html><body>
                        <a href="mailto:contact@example.com">Contact Us</a>
                    </body></html>
                    """;

            Contact contact = ContactInfoParser.parse(html);

            assertNotNull(contact);
            assertTrue(contact.hasContactInfo());
            assertTrue(contact.getEmails().contains("contact@example.com"));
        }

        @Test
        @DisplayName("should handle mailto links with query parameters")
        void shouldHandleMailtoWithQueryParams() {
            String html = """
                    <html><body>
                        <a href="mailto:info@test.com?subject=Hello&body=Test">Email Us</a>
                    </body></html>
                    """;

            Contact contact = ContactInfoParser.parse(html);

            assertNotNull(contact);
            assertTrue(contact.hasContactInfo());
            assertTrue(contact.getEmails().contains("info@test.com"));
        }

        @Test
        @DisplayName("should deduplicate emails")
        void shouldDeduplicateEmails() {
            String html = """
                    <html><body>
                        <p>Email: duplicate@example.com</p>
                        <p>Also: duplicate@example.com</p>
                        <a href="mailto:duplicate@example.com">Contact</a>
                    </body></html>
                    """;

            Contact contact = ContactInfoParser.parse(html);

            assertNotNull(contact);
            assertTrue(contact.hasContactInfo());
            assertEquals(1, contact.getEmails().size());
            assertTrue(contact.getEmails().contains("duplicate@example.com"));
        }

        @Test
        @DisplayName("should handle various email formats")
        void shouldHandleVariousEmailFormats() {
            String html = """
                    <html><body>
                        <p>user.name@domain.com</p>
                        <p>user+tag@domain.co.uk</p>
                        <p>user_name@sub.domain.org</p>
                    </body></html>
                    """;

            Contact contact = ContactInfoParser.parse(html);

            assertNotNull(contact);
            assertTrue(contact.hasContactInfo());
            assertTrue(contact.getEmails().size() >= 2);
        }

        @Test
        @DisplayName("should reject invalid emails")
        void shouldRejectInvalidEmails() {
            String html = """
                    <html><body>
                        <p>invalid-email</p>
                        <p>@nodomain.com</p>
                        <p>noatsign.com</p>
                    </body></html>
                    """;

            Contact contact = ContactInfoParser.parse(html);

            assertNotNull(contact);
            assertFalse(contact.hasContactInfo());
            assertTrue(contact.getEmails().isEmpty());
        }

        @Test
        @DisplayName("should handle HTML without any contact info")
        void shouldHandleHtmlWithoutContactInfo() {
            String html = """
                    <html>
                    <head><title>No Contact Page</title></head>
                    <body>
                        <h1>Welcome</h1>
                        <p>This page has no contact information.</p>
                    </body>
                    </html>
                    """;

            Contact contact = ContactInfoParser.parse(html);

            assertNotNull(contact);
            assertFalse(contact.hasContactInfo());
            assertTrue(contact.getEmails().isEmpty());
        }

        @Test
        @DisplayName("should handle malformed HTML gracefully")
        void shouldHandleMalformedHtml() {
            String html = "<html><body><p>Email: test@example.com</p><div><span>";

            Contact contact = ContactInfoParser.parse(html);

            assertNotNull(contact);
            assertTrue(contact.hasContactInfo());
            assertTrue(contact.getEmails().contains("test@example.com"));
        }

        @Test
        @DisplayName("should extract email from complex nested HTML")
        void shouldExtractEmailFromNestedHtml() {
            String html = """
                    <html>
                    <body>
                        <div class="footer">
                            <div class="contact-section">
                                <ul>
                                    <li>
                                        <span class="icon"></span>
                                        <a href="mailto:nested@deep.com">nested@deep.com</a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </body>
                    </html>
                    """;

            Contact contact = ContactInfoParser.parse(html);

            assertNotNull(contact);
            assertTrue(contact.hasContactInfo());
            assertTrue(contact.getEmails().contains("nested@deep.com"));
        }
    }

    @Nested
    @DisplayName("Email Pattern Tests")
    class EmailPatternTests {

        @Test
        @DisplayName("EMAIL_PATTERN should match standard emails")
        void patternShouldMatchStandardEmails() {
            assertTrue(ContactInfoParser.EMAIL_PATTERN.matcher("test@example.com").find());
            assertTrue(ContactInfoParser.EMAIL_PATTERN.matcher("user.name@domain.org").find());
            assertTrue(ContactInfoParser.EMAIL_PATTERN.matcher("user+tag@company.net").find());
        }

        @Test
        @DisplayName("EMAIL_PATTERN should be case insensitive")
        void patternShouldBeCaseInsensitive() {
            assertTrue(ContactInfoParser.EMAIL_PATTERN.matcher("TEST@EXAMPLE.COM").find());
            assertTrue(ContactInfoParser.EMAIL_PATTERN.matcher("User@Domain.Org").find());
        }
    }
}

package ir.netpick.mailmine.scrape.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Contact Model Tests")
class ContactTest {

    private Contact contact;

    @BeforeEach
    void setUp() {
        contact = new Contact();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("default constructor should initialize empty emails set")
        void defaultConstructorShouldInitializeEmptyEmails() {
            Contact newContact = new Contact();

            assertNotNull(newContact.getEmails());
            assertTrue(newContact.getEmails().isEmpty());
        }

        @Test
        @DisplayName("constructor with ScrapeData should set scrapeData")
        void constructorWithScrapeDataShouldSetIt() {
            ScrapeData scrapeData = new ScrapeData();

            Contact newContact = new Contact(scrapeData);

            assertNotNull(newContact.getScrapeData());
            assertEquals(scrapeData, newContact.getScrapeData());
        }
    }

    @Nested
    @DisplayName("hasContactInfo() method")
    class HasContactInfoMethod {

        @Test
        @DisplayName("should return false when emails is empty")
        void shouldReturnFalseWhenEmailsEmpty() {
            assertFalse(contact.hasContactInfo());
        }

        @Test
        @DisplayName("should return true when emails is not empty")
        void shouldReturnTrueWhenEmailsNotEmpty() {
            contact.getEmails().add("test@example.com");

            assertTrue(contact.hasContactInfo());
        }

        @Test
        @DisplayName("should return true with multiple emails")
        void shouldReturnTrueWithMultipleEmails() {
            contact.getEmails().add("one@example.com");
            contact.getEmails().add("two@example.com");
            contact.getEmails().add("three@example.com");

            assertTrue(contact.hasContactInfo());
        }
    }

    @Nested
    @DisplayName("Emails Set Tests")
    class EmailsSetTests {

        @Test
        @DisplayName("should add email to set")
        void shouldAddEmailToSet() {
            contact.getEmails().add("test@example.com");

            assertEquals(1, contact.getEmails().size());
            assertTrue(contact.getEmails().contains("test@example.com"));
        }

        @Test
        @DisplayName("should not add duplicate emails")
        void shouldNotAddDuplicateEmails() {
            contact.getEmails().add("duplicate@example.com");
            contact.getEmails().add("duplicate@example.com");

            assertEquals(1, contact.getEmails().size());
        }

        @Test
        @DisplayName("should remove email from set")
        void shouldRemoveEmailFromSet() {
            contact.getEmails().add("removeme@example.com");
            contact.getEmails().remove("removeme@example.com");

            assertTrue(contact.getEmails().isEmpty());
        }
    }

    @Nested
    @DisplayName("toString() method")
    class ToStringMethod {

        @Test
        @DisplayName("should include emails in toString")
        void shouldIncludeEmailsInToString() {
            contact.getEmails().add("test@example.com");

            String result = contact.toString();

            assertTrue(result.contains("test@example.com"));
            assertTrue(result.contains("emails"));
        }
    }
}

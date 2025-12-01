package ir.netpick.mailmine.scrape.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LinkResult Model Tests")
class LinkResultTest {

    private LinkResult linkResult;

    @BeforeEach
    void setUp() {
        linkResult = new LinkResult();
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get title")
        void shouldSetAndGetTitle() {
            linkResult.setTitle("Test Title");

            assertEquals("Test Title", linkResult.getTitle());
        }

        @Test
        @DisplayName("should set and get link")
        void shouldSetAndGetLink() {
            linkResult.setLink("https://example.com");

            assertEquals("https://example.com", linkResult.getLink());
        }

        @Test
        @DisplayName("should set and get snippet")
        void shouldSetAndGetSnippet() {
            linkResult.setSnippet("This is a snippet");

            assertEquals("This is a snippet", linkResult.getSnippet());
        }

        @Test
        @DisplayName("should handle null values")
        void shouldHandleNullValues() {
            linkResult.setTitle(null);
            linkResult.setLink(null);
            linkResult.setSnippet(null);

            assertNull(linkResult.getTitle());
            assertNull(linkResult.getLink());
            assertNull(linkResult.getSnippet());
        }

        @Test
        @DisplayName("should handle empty strings")
        void shouldHandleEmptyStrings() {
            linkResult.setTitle("");
            linkResult.setLink("");
            linkResult.setSnippet("");

            assertEquals("", linkResult.getTitle());
            assertEquals("", linkResult.getLink());
            assertEquals("", linkResult.getSnippet());
        }
    }

    @Nested
    @DisplayName("Field Independence Tests")
    class FieldIndependenceTests {

        @Test
        @DisplayName("fields should be independent")
        void fieldsShouldBeIndependent() {
            linkResult.setTitle("Title");
            linkResult.setLink("https://link.com");
            linkResult.setSnippet("Snippet");

            // Modify one field
            linkResult.setTitle("New Title");

            // Others should remain unchanged
            assertEquals("New Title", linkResult.getTitle());
            assertEquals("https://link.com", linkResult.getLink());
            assertEquals("Snippet", linkResult.getSnippet());
        }
    }
}

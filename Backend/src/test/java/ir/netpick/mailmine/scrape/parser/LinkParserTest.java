package ir.netpick.mailmine.scrape.parser;

import ir.netpick.mailmine.scrape.model.LinkResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LinkParser Tests")
class LinkParserTest {

    @Nested
    @DisplayName("parse() method")
    class ParseMethod {

        @Test
        @DisplayName("should return empty list when JSON is null")
        void shouldReturnEmptyListWhenJsonIsNull() {
            List<LinkResult> results = LinkParser.parse(null);

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when JSON is empty")
        void shouldReturnEmptyListWhenJsonIsEmpty() {
            List<LinkResult> results = LinkParser.parse("");

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when JSON is blank")
        void shouldReturnEmptyListWhenJsonIsBlank() {
            List<LinkResult> results = LinkParser.parse("   \n\t  ");

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when no items field exists")
        void shouldReturnEmptyListWhenNoItemsField() {
            String json = """
                    {
                        "kind": "customsearch#search",
                        "queries": {}
                    }
                    """;

            List<LinkResult> results = LinkParser.parse(json);

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when items is empty array")
        void shouldReturnEmptyListWhenItemsIsEmpty() {
            String json = """
                    {
                        "kind": "customsearch#search",
                        "items": []
                    }
                    """;

            List<LinkResult> results = LinkParser.parse(json);

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should parse single search result item")
        void shouldParseSingleItem() {
            String json = """
                    {
                        "kind": "customsearch#search",
                        "items": [
                            {
                                "title": "Example Title",
                                "link": "https://example.com",
                                "snippet": "This is the snippet text."
                            }
                        ]
                    }
                    """;

            List<LinkResult> results = LinkParser.parse(json);

            assertNotNull(results);
            assertEquals(1, results.size());

            LinkResult result = results.get(0);
            assertEquals("Example Title", result.getTitle());
            assertEquals("https://example.com", result.getLink());
            assertEquals("This is the snippet text.", result.getSnippet());
        }

        @Test
        @DisplayName("should parse multiple search result items")
        void shouldParseMultipleItems() {
            String json = """
                    {
                        "kind": "customsearch#search",
                        "items": [
                            {
                                "title": "First Result",
                                "link": "https://first.com",
                                "snippet": "First snippet"
                            },
                            {
                                "title": "Second Result",
                                "link": "https://second.com",
                                "snippet": "Second snippet"
                            },
                            {
                                "title": "Third Result",
                                "link": "https://third.com",
                                "snippet": "Third snippet"
                            }
                        ]
                    }
                    """;

            List<LinkResult> results = LinkParser.parse(json);

            assertNotNull(results);
            assertEquals(3, results.size());

            assertEquals("First Result", results.get(0).getTitle());
            assertEquals("https://first.com", results.get(0).getLink());

            assertEquals("Second Result", results.get(1).getTitle());
            assertEquals("https://second.com", results.get(1).getLink());

            assertEquals("Third Result", results.get(2).getTitle());
            assertEquals("https://third.com", results.get(2).getLink());
        }

        @Test
        @DisplayName("should handle items with missing optional fields")
        void shouldHandleMissingOptionalFields() {
            String json = """
                    {
                        "items": [
                            {
                                "link": "https://example.com"
                            }
                        ]
                    }
                    """;

            List<LinkResult> results = LinkParser.parse(json);

            assertNotNull(results);
            assertEquals(1, results.size());

            LinkResult result = results.get(0);
            assertEquals("https://example.com", result.getLink());
            assertNull(result.getTitle());
            assertNull(result.getSnippet());
        }

        @Test
        @DisplayName("should skip items without link")
        void shouldSkipItemsWithoutLink() {
            String json = """
                    {
                        "items": [
                            {
                                "title": "No Link Item",
                                "snippet": "This has no link"
                            },
                            {
                                "title": "Has Link",
                                "link": "https://example.com",
                                "snippet": "This has a link"
                            }
                        ]
                    }
                    """;

            List<LinkResult> results = LinkParser.parse(json);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("https://example.com", results.get(0).getLink());
        }

        @Test
        @DisplayName("should skip items with blank link")
        void shouldSkipItemsWithBlankLink() {
            String json = """
                    {
                        "items": [
                            {
                                "title": "Blank Link",
                                "link": "   ",
                                "snippet": "Link is blank"
                            },
                            {
                                "title": "Valid Link",
                                "link": "https://valid.com",
                                "snippet": "Valid link"
                            }
                        ]
                    }
                    """;

            List<LinkResult> results = LinkParser.parse(json);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("https://valid.com", results.get(0).getLink());
        }

        @Test
        @DisplayName("should handle null items in array")
        void shouldHandleNullItemsInArray() {
            String json = """
                    {
                        "items": [
                            null,
                            {
                                "title": "Valid Item",
                                "link": "https://valid.com",
                                "snippet": "Valid snippet"
                            },
                            null
                        ]
                    }
                    """;

            List<LinkResult> results = LinkParser.parse(json);

            assertNotNull(results);
            assertEquals(1, results.size());
            assertEquals("https://valid.com", results.get(0).getLink());
        }

        @Test
        @DisplayName("should handle invalid JSON gracefully")
        void shouldHandleInvalidJsonGracefully() {
            String invalidJson = "{ this is not valid json }}}";

            List<LinkResult> results = LinkParser.parse(invalidJson);

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should handle items field that is not an array")
        void shouldHandleItemsNotArray() {
            String json = """
                    {
                        "items": "not an array"
                    }
                    """;

            List<LinkResult> results = LinkParser.parse(json);

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should handle real Google API response structure")
        void shouldHandleRealGoogleApiResponse() {
            String json = """
                    {
                        "kind": "customsearch#search",
                        "url": {
                            "type": "application/json",
                            "template": "https://www.googleapis.com/customsearch/v1?q={searchTerms}"
                        },
                        "queries": {
                            "request": [{"totalResults": "100"}]
                        },
                        "searchInformation": {
                            "searchTime": 0.5,
                            "totalResults": "100"
                        },
                        "items": [
                            {
                                "kind": "customsearch#result",
                                "title": "Company Contact Page",
                                "htmlTitle": "<b>Company</b> Contact Page",
                                "link": "https://company.com/contact",
                                "displayLink": "company.com",
                                "snippet": "Contact us at our office.",
                                "htmlSnippet": "Contact us at our <b>office</b>.",
                                "formattedUrl": "https://company.com/contact",
                                "pagemap": {}
                            }
                        ]
                    }
                    """;

            List<LinkResult> results = LinkParser.parse(json);

            assertNotNull(results);
            assertEquals(1, results.size());

            LinkResult result = results.get(0);
            assertEquals("Company Contact Page", result.getTitle());
            assertEquals("https://company.com/contact", result.getLink());
            assertEquals("Contact us at our office.", result.getSnippet());
        }
    }
}

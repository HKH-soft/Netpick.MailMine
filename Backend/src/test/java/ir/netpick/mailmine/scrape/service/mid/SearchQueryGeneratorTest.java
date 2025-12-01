package ir.netpick.mailmine.scrape.service.mid;

import ir.netpick.mailmine.ai.service.GeminiService;
import ir.netpick.mailmine.scrape.dto.SearchQueryRequest;
import ir.netpick.mailmine.scrape.model.SearchQuery;
import ir.netpick.mailmine.scrape.service.base.SearchQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchQueryGenerator Tests")
class SearchQueryGeneratorTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private SearchQueryService searchQueryService;

    @InjectMocks
    private SearchQueryGenerator searchQueryGenerator;

    @Nested
    @DisplayName("generateQueries() method")
    class GenerateQueriesMethod {

        @Test
        @DisplayName("should generate queries from AI response")
        void shouldGenerateQueriesFromAiResponse() {
            String aiResponse = """
                    software developer contact page
                    software engineer email directory
                    tech company staff listing
                    """;

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            List<String> queries = searchQueryGenerator.generateQueries("tech", "developers", 3);

            assertNotNull(queries);
            assertEquals(3, queries.size());
            assertTrue(queries.contains("software developer contact page"));
            assertTrue(queries.contains("software engineer email directory"));
            assertTrue(queries.contains("tech company staff listing"));
        }

        @Test
        @DisplayName("should filter out empty lines")
        void shouldFilterOutEmptyLines() {
            String aiResponse = """
                    query one

                    query two

                    """;

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            List<String> queries = searchQueryGenerator.generateQueries("topic", "target", 2);

            assertEquals(2, queries.size());
            assertTrue(queries.contains("query one"));
            assertTrue(queries.contains("query two"));
        }

        @Test
        @DisplayName("should filter out numbered lines")
        void shouldFilterOutNumberedLines() {
            String aiResponse = """
                    1. numbered query one
                    2. numbered query two
                    clean query
                    """;

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            List<String> queries = searchQueryGenerator.generateQueries("topic", "target", 3);

            assertEquals(1, queries.size());
            assertTrue(queries.contains("clean query"));
        }

        @Test
        @DisplayName("should filter out bullet point lines")
        void shouldFilterOutBulletPointLines() {
            String aiResponse = """
                    - bullet query one
                    * asterisk query
                    clean query here
                    """;

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            List<String> queries = searchQueryGenerator.generateQueries("topic", "target", 3);

            assertEquals(1, queries.size());
            assertTrue(queries.contains("clean query here"));
        }

        @Test
        @DisplayName("should filter out very short lines")
        void shouldFilterOutShortLines() {
            String aiResponse = """
                    ab
                    abc
                    valid query here
                    """;

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            List<String> queries = searchQueryGenerator.generateQueries("topic", "target", 3);

            assertEquals(1, queries.size());
            assertTrue(queries.contains("valid query here"));
        }

        @Test
        @DisplayName("should return empty list when AI returns null")
        void shouldReturnEmptyListWhenAiReturnsNull() {
            when(geminiService.generateText(anyString())).thenReturn(null);

            List<String> queries = searchQueryGenerator.generateQueries("topic", "target", 3);

            assertNotNull(queries);
            assertTrue(queries.isEmpty());
        }

        @Test
        @DisplayName("should return empty list when AI returns blank")
        void shouldReturnEmptyListWhenAiReturnsBlank() {
            when(geminiService.generateText(anyString())).thenReturn("   \n\n  ");

            List<String> queries = searchQueryGenerator.generateQueries("topic", "target", 3);

            assertNotNull(queries);
            assertTrue(queries.isEmpty());
        }

        @Test
        @DisplayName("should throw RuntimeException when AI service fails")
        void shouldThrowExceptionWhenAiServiceFails() {
            when(geminiService.generateText(anyString()))
                    .thenThrow(new RuntimeException("API error"));

            assertThrows(RuntimeException.class, () -> searchQueryGenerator.generateQueries("topic", "target", 3));
        }

        @Test
        @DisplayName("should include topic and target in prompt")
        void shouldIncludeTopicAndTargetInPrompt() {
            when(geminiService.generateText(anyString())).thenReturn("query");

            ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

            searchQueryGenerator.generateQueries("healthcare", "doctors", 5);

            verify(geminiService).generateText(promptCaptor.capture());
            String prompt = promptCaptor.getValue();

            assertTrue(prompt.contains("healthcare"));
            assertTrue(prompt.contains("doctors"));
            assertTrue(prompt.contains("5"));
        }
    }

    @Nested
    @DisplayName("generateAndSaveQueries() method")
    class GenerateAndSaveQueriesMethod {

        @Test
        @DisplayName("should save generated queries to database")
        void shouldSaveGeneratedQueries() {
            String aiResponse = """
                    query one
                    query two
                    """;

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            SearchQuery savedQuery = new SearchQuery();
            when(searchQueryService.createSearchQuery(any(SearchQueryRequest.class)))
                    .thenReturn(savedQuery);

            List<SearchQuery> result = searchQueryGenerator.generateAndSaveQueries("topic", "target", 2);

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(searchQueryService, times(2)).createSearchQuery(any(SearchQueryRequest.class));
        }

        @Test
        @DisplayName("should continue saving other queries when one fails")
        void shouldContinueWhenOneSaveFails() {
            String aiResponse = """
                    query one
                    query two
                    query three
                    """;

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            SearchQuery savedQuery = new SearchQuery();
            when(searchQueryService.createSearchQuery(any(SearchQueryRequest.class)))
                    .thenReturn(savedQuery)
                    .thenThrow(new RuntimeException("Save failed"))
                    .thenReturn(savedQuery);

            List<SearchQuery> result = searchQueryGenerator.generateAndSaveQueries("topic", "target", 3);

            assertEquals(2, result.size());
            verify(searchQueryService, times(3)).createSearchQuery(any(SearchQueryRequest.class));
        }

        @Test
        @DisplayName("should set correct properties on SearchQueryRequest")
        void shouldSetCorrectPropertiesOnRequest() {
            String aiResponse = "my search query";

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            SearchQuery savedQuery = new SearchQuery();
            when(searchQueryService.createSearchQuery(any(SearchQueryRequest.class)))
                    .thenReturn(savedQuery);

            ArgumentCaptor<SearchQueryRequest> requestCaptor = ArgumentCaptor.forClass(SearchQueryRequest.class);

            searchQueryGenerator.generateAndSaveQueries("my-topic", "target", 1);

            verify(searchQueryService).createSearchQuery(requestCaptor.capture());
            SearchQueryRequest request = requestCaptor.getValue();

            assertEquals("my search query", request.sentence());
            assertEquals(0, request.linkCount());
            assertTrue(request.description().contains("my-topic"));
        }
    }

    @Nested
    @DisplayName("generateVariations() method")
    class GenerateVariationsMethod {

        @Test
        @DisplayName("should generate variations of original query")
        void shouldGenerateVariations() {
            String aiResponse = """
                    software engineer jobs
                    developer positions available
                    coding job openings
                    """;

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            List<String> variations = searchQueryGenerator.generateVariations("software jobs", 3);

            assertNotNull(variations);
            assertEquals(3, variations.size());
        }

        @Test
        @DisplayName("should include original query in prompt")
        void shouldIncludeOriginalQueryInPrompt() {
            when(geminiService.generateText(anyString())).thenReturn("variation");

            ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

            searchQueryGenerator.generateVariations("original query here", 5);

            verify(geminiService).generateText(promptCaptor.capture());
            assertTrue(promptCaptor.getValue().contains("original query here"));
        }
    }

    @Nested
    @DisplayName("generateSiteQueries() method")
    class GenerateSiteQueriesMethod {

        @Test
        @DisplayName("should generate site-restricted queries")
        void shouldGenerateSiteQueries() {
            String aiResponse = """
                    site:linkedin.com software developer
                    site:linkedin.com tech jobs
                    """;

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            List<String> queries = searchQueryGenerator.generateSiteQueries("tech", "linkedin.com", 2);

            assertNotNull(queries);
            assertEquals(2, queries.size());
        }

        @Test
        @DisplayName("should include site restriction in prompt")
        void shouldIncludeSiteInPrompt() {
            when(geminiService.generateText(anyString())).thenReturn("query");

            ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

            searchQueryGenerator.generateSiteQueries("topic", "example.com", 3);

            verify(geminiService).generateText(promptCaptor.capture());
            String prompt = promptCaptor.getValue();
            assertTrue(prompt.contains("example.com"));
            assertTrue(prompt.contains("site:"));
        }
    }

    @Nested
    @DisplayName("generateEmailQueries() method")
    class GenerateEmailQueriesMethod {

        @Test
        @DisplayName("should generate email-finding queries with region")
        void shouldGenerateEmailQueriesWithRegion() {
            String aiResponse = """
                    healthcare contact email New York
                    hospital staff directory NY
                    """;

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            List<String> queries = searchQueryGenerator.generateEmailQueries("healthcare", "New York", 2);

            assertNotNull(queries);
            assertEquals(2, queries.size());
        }

        @Test
        @DisplayName("should generate email-finding queries without region")
        void shouldGenerateEmailQueriesWithoutRegion() {
            String aiResponse = "global healthcare contacts";

            when(geminiService.generateText(anyString())).thenReturn(aiResponse);

            List<String> queries = searchQueryGenerator.generateEmailQueries("healthcare", null, 1);

            assertNotNull(queries);
            assertEquals(1, queries.size());
        }

        @Test
        @DisplayName("should handle blank region as global")
        void shouldHandleBlankRegionAsGlobal() {
            when(geminiService.generateText(anyString())).thenReturn("query");

            ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);

            searchQueryGenerator.generateEmailQueries("tech", "  ", 1);

            verify(geminiService).generateText(promptCaptor.capture());
            // Should not throw, should work with blank region
        }
    }
}

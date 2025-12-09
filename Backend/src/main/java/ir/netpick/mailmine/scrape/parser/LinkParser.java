package ir.netpick.mailmine.scrape.parser;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ir.netpick.mailmine.scrape.model.LinkResult;

@Slf4j
@Service
public class LinkParser {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<LinkResult> parse(String jsonBody) {
        List<LinkResult> results = new ArrayList<>();

        if (jsonBody == null || jsonBody.isBlank()) {
            log.warn("Empty or null Google API response");
            return results;
        }

        try {
            JsonNode jsonRoot = objectMapper.readTree(jsonBody);

            if (!jsonRoot.has("items") || !jsonRoot.get("items").isArray()) {
                log.info("No 'items' field found in Google response");
                return results;
            }

            for (JsonNode searchResultItem : jsonRoot.get("items")) {
                if (searchResultItem == null || searchResultItem.isNull())
                    continue;

                String link = getSafeText(searchResultItem, "link");
                String title = getSafeText(searchResultItem, "title");
                String snippet = getSafeText(searchResultItem, "snippet");

                if (link != null && !link.isBlank()) {
                    LinkResult result = new LinkResult();
                    result.setLink(link);
                    result.setTitle(title);
                    result.setSnippet(snippet);
                    results.add(result);
                }
            }

        } catch (Exception e) {
            log.error("Failed to parse Google API response: {}", e.getMessage());
        }

        return results;
    }

    private static String getSafeText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

}

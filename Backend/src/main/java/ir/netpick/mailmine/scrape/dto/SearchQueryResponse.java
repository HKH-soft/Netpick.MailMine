package ir.netpick.mailmine.scrape.dto;

import java.time.LocalDateTime;

public record SearchQueryResponse(
        String sentence,
        int linkCount,
        String description,
        LocalDateTime created_at,
        LocalDateTime updatedAt) {

}

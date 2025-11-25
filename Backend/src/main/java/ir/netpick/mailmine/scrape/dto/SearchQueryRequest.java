package ir.netpick.mailmine.scrape.dto;

public record SearchQueryRequest(
        String sentence,
        int linkCount,
        String description) {

}

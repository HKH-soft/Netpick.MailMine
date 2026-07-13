package ir.netpick.mailmine.ai.controller;

import ir.netpick.mailmine.ai.service.DraftReplyService;
import ir.netpick.mailmine.ai.service.EmailSummaryService;
import ir.netpick.mailmine.ai.service.SentimentAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class AiController {

    private final EmailSummaryService emailSummaryService;
    private final DraftReplyService draftReplyService;
    private final SentimentAnalysisService sentimentAnalysisService;

    // === Summaries ===

    @GetMapping("/email/{emailId}/summary")
    public CompletableFuture<ResponseEntity<String>> summarizeEmail(@PathVariable UUID emailId) {
        return emailSummaryService.summarizeEmail(emailId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/thread/{threadId}/summary")
    public CompletableFuture<ResponseEntity<String>> summarizeThread(@PathVariable String threadId) {
        return emailSummaryService.summarizeThread(threadId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/email/{emailId}/status")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getCustomerStatus(
            @PathVariable UUID emailId) {
        return emailSummaryService.generateCustomerStatus(emailId)
                .thenApply(ResponseEntity::ok);
    }

    // === Draft Replies ===

    @GetMapping("/email/{emailId}/draft")
    public CompletableFuture<ResponseEntity<String>> generateDraft(@PathVariable UUID emailId) {
        return draftReplyService.generateDraft(emailId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/email/{emailId}/draft/template/{templateId}")
    public CompletableFuture<ResponseEntity<String>> generateDraftWithTemplate(
            @PathVariable UUID emailId,
            @PathVariable UUID templateId) {
        return draftReplyService.generateDraftWithTemplate(emailId, templateId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/email/{emailId}/subject-suggestions")
    public CompletableFuture<ResponseEntity<List<String>>> generateSubjectSuggestions(
            @PathVariable UUID emailId) {
        return draftReplyService.generateSubjectSuggestions(emailId)
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/draft/improve")
    public CompletableFuture<ResponseEntity<String>> improveDraft(
            @RequestBody Map<String, String> request) {
        return draftReplyService.improveDraft(
                request.get("draft"),
                request.get("instructions"))
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/email/{emailId}/draft/contextual")
    public CompletableFuture<ResponseEntity<String>> generateContextualReply(
            @PathVariable UUID emailId,
            @RequestParam(defaultValue = "") String companyContext) {
        return draftReplyService.generateContextualReply(emailId, companyContext)
                .thenApply(ResponseEntity::ok);
    }

    // === Sentiment ===

    @GetMapping("/email/{emailId}/sentiment")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> analyzeSentiment(
            @PathVariable UUID emailId) {
        return sentimentAnalysisService.analyzeEmail(emailId)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/thread/{threadId}/sentiment-trend")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> analyzeThreadSentiment(
            @PathVariable String threadId) {
        return sentimentAnalysisService.analyzeThreadTrend(threadId)
                .thenApply(ResponseEntity::ok);
    }
}

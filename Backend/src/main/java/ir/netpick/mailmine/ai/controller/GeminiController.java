package ir.netpick.mailmine.ai.controller;

import ir.netpick.mailmine.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;
    
    private static final int MAX_PROMPT_LENGTH = 5000; // Reasonable limit for AI prompts

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateText(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        
        // Validate prompt is not null or blank
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }
        
        // Validate prompt length to prevent abuse
        if (prompt.length() > MAX_PROMPT_LENGTH) {
            return ResponseEntity.badRequest().body(
                Map.of("error", String.format("Prompt exceeds maximum length of %d characters", MAX_PROMPT_LENGTH))
            );
        }
        
        String response = geminiService.generateText(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }

    @PostMapping("/generate/short")
    public ResponseEntity<Map<String, String>> generateShortText(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        
        // Validate prompt is not null or blank
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }
        
        // Validate prompt length to prevent abuse
        if (prompt.length() > MAX_PROMPT_LENGTH) {
            return ResponseEntity.badRequest().body(
                Map.of("error", String.format("Prompt exceeds maximum length of %d characters", MAX_PROMPT_LENGTH))
            );
        }
        
        String response = geminiService.generateShortText(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }
}

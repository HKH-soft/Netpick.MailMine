package ir.netpick.mailmine.ai.controller;

import ir.netpick.mailmine.ai.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class GeminiController {

    private final GeminiService geminiService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PREMIUM')")
    public ResponseEntity<Map<String, String>> generateText(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }
        String response = geminiService.generateText(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }

    @PostMapping("/generate/short")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PREMIUM')")
    public ResponseEntity<Map<String, String>> generateShortText(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }
        String response = geminiService.generateShortText(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }
}

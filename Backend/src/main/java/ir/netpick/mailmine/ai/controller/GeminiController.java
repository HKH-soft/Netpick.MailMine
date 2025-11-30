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

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateText(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }
        String response = geminiService.generateText(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }

    @PostMapping("/generate/short")
    public ResponseEntity<Map<String, String>> generateShortText(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt is required"));
        }
        String response = geminiService.generateShortText(prompt);
        return ResponseEntity.ok(Map.of("response", response));
    }
}

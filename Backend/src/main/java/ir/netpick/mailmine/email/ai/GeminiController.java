package ir.netpick.mailmine.email.ai;

import com.google.gson.Gson;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class GeminiController {
    private final GenerateTextFromTextInput gemini;
    private final Gson gson;

    public GeminiController(GenerateTextFromTextInput gemini, Gson gson) {
        this.gemini = gemini;
        this.gson = gson;
    }

    @GetMapping("text")
    public ResponseEntity<String> text(@RequestBody String text) {
        return ResponseEntity.ok().body(gson.toJson(gemini.generateText(text)));
    }

}

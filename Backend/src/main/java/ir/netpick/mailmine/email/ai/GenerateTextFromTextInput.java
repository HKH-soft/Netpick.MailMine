package ir.netpick.mailmine.email.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.stereotype.Service;

@Service
public class GenerateTextFromTextInput {
  public String generateText(String text) {
    Client client = new Client();

    GenerateContentResponse response = client.models.generateContent(
        "gemini-2.5-flash",
        text + " say it in a few words",
        null);

    client.close();

    return response.text();
  }
}
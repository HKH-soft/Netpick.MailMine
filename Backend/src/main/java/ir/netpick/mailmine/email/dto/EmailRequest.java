package ir.netpick.mailmine.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {

    private String recipient;
    private String subject;
    private String body;
    private String attachment;

    // For mass email
    private List<String> recipients;
    private String templateName;
}

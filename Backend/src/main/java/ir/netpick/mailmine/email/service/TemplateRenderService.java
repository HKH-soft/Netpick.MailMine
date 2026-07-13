package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.email.model.EmailTemplate;
import ir.netpick.mailmine.email.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TemplateRenderService {

    private final EmailTemplateRepository templateRepository;

    /**
     * Renders a template with given variables.
     * Variables use {{variableName}} syntax.
     * Built-in variables: {{currentDate}}, {{currentTime}}, {{firstName}}, {{lastName}}
     */
    public String render(String templateContent, Map<String, String> variables) {
        String result = templateContent;

        // Built-in variables
        LocalDateTime now = LocalDateTime.now();
        result = result.replace("{{currentDate}}", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        result = result.replace("{{currentTime}}", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        result = result.replace("{{currentDateTime}}", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // User-provided variables
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        return result;
    }

    /**
     * Render a template by ID with variables
     */
    public RenderedTemplate renderById(UUID templateId, Map<String, String> variables) {
        EmailTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found: " + templateId));

        String renderedSubject = render(template.getSubjectTemplate(), variables);
        String renderedBody = render(template.getBodyTemplate(), variables);

        RenderedTemplate result = new RenderedTemplate();
        result.templateId = templateId;
        result.templateName = template.getName();
        result.subject = renderedSubject;
        result.bodyHtml = renderedBody;
        result.variables = variables;
        return result;
    }

    public static class RenderedTemplate {
        public UUID templateId;
        public String templateName;
        public String subject;
        public String bodyHtml;
        public Map<String, String> variables;
    }
}

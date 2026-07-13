package ir.netpick.mailmine.email.service;

import ir.netpick.mailmine.common.exception.ResourceNotFoundException;
import ir.netpick.mailmine.email.model.EmailMessage;
import ir.netpick.mailmine.email.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final EmailMessageRepository emailMessageRepository;

    @Value("${mail.attachments.upload-dir:./uploads/attachments}")
    private String uploadDir;

    @Value("${mail.attachments.max-size:10485760}")  // 10MB default
    private long maxFileSize;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "json", "xml",
            "png", "jpg", "jpeg", "gif", "webp",
            "zip", "rar", "7z"
    );

    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "sh", "ps1", "vbs", "js", "msi"
    );

    /**
     * Save attachment for an email
     */
    public Map<String, Object> saveAttachment(UUID emailId, MultipartFile file) throws IOException {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));

        validateFile(file);

        String extension = getFileExtension(file.getOriginalFilename());
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Path.of(uploadDir, datePath);

        Files.createDirectories(uploadPath);

        String filename = UUID.randomUUID() + "." + extension;
        Path filePath = uploadPath.resolve(filename);

        file.transferTo(filePath.toFile());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("filename", file.getOriginalFilename());
        result.put("storedFilename", filename);
        result.put("path", filePath.toString());
        result.put("size", file.getSize());
        result.put("contentType", file.getContentType());
        result.put("emailId", emailId.toString());

        email.setHasAttachments(true);
        emailMessageRepository.save(email);

        return result;
    }

    /**
     * Get attachments for an email
     */
    public List<Map<String, Object>> getAttachments(UUID emailId) {
        EmailMessage email = emailMessageRepository.findById(emailId)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found: " + emailId));

        // Scan upload directory for attachments belonging to this email
        List<Map<String, Object>> attachments = new ArrayList<>();
        try {
            Path emailDir = Path.of(uploadDir);
            if (Files.exists(emailDir)) {
                Files.walk(emailDir)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().contains(emailId.toString()))
                        .forEach(path -> {
                            Map<String, Object> info = new LinkedHashMap<>();
                            info.put("filename", path.getFileName().toString());
                            info.put("path", path.toString());
                            info.put("size", path.toFile().length());
                            attachments.add(info);
                        });
            }
        } catch (IOException e) {
            log.error("Failed to scan attachments for email {}: {}", emailId, e.getMessage());
        }

        return attachments;
    }

    /**
     * Search attachments by keyword
     */
    public List<Map<String, Object>> searchAttachments(String query) {
        List<Map<String, Object>> results = new ArrayList<>();
        try {
            Path rootPath = Path.of(uploadDir);
            if (Files.exists(rootPath)) {
                Files.walk(rootPath)
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString()
                                .toLowerCase().contains(query.toLowerCase()))
                        .limit(50)
                        .forEach(path -> {
                            Map<String, Object> info = new LinkedHashMap<>();
                            info.put("filename", path.getFileName().toString());
                            info.put("path", path.toString());
                            info.put("size", path.toFile().length());
                            results.add(info);
                        });
            }
        } catch (IOException e) {
            log.error("Failed to search attachments: {}", e.getMessage());
        }

        return results;
    }

    /**
     * Delete attachment
     */
    public void deleteAttachment(String storedFilename) throws IOException {
        Files.deleteIfExists(Path.of(uploadDir, storedFilename));
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum of " + maxFileSize + " bytes");
        }

        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();

        if (BLOCKED_EXTENSIONS.contains(extension)) {
            throw new SecurityException("File type not allowed: " + extension);
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file type: " + extension);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}

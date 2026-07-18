package ir.netpick.platform.taskfarm.service;

import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.taskfarm.model.Attachment;
import ir.netpick.platform.taskfarm.repository.AttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    @Value("${taskfarm.upload.dir:uploads/taskfarm/attachments/}")
    private String uploadDir;

    public List<Attachment> getByTask(UUID taskId) {
        return attachmentRepository.findByTaskIdAndDeletedFalse(taskId);
    }

    public Attachment getById(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
    }

    public Attachment upload(UUID taskId, MultipartFile file, UUID uploadedById) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);

        Attachment attachment = new Attachment();
        attachment.setId(null);
        attachment.setTaskId(taskId);
        attachment.setFilename(filename);
        attachment.setOriginalFilename(file.getOriginalFilename());
        attachment.setFilePath(filePath.toString());
        attachment.setFileSize(file.getSize());
        attachment.setContentType(file.getContentType());
        attachment.setUploadedById(uploadedById);

        return attachmentRepository.save(attachment);
    }

    public void delete(UUID attachmentId) {
        Attachment attachment = getById(attachmentId);
        Path filePath = Paths.get(attachment.getFilePath());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
        attachmentRepository.delete(attachment);
    }
}
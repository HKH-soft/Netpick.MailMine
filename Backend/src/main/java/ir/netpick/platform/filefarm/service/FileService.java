package ir.netpick.platform.filefarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.constants.GeneralConstants;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.core.utils.PageDTOMapper;
import ir.netpick.platform.filefarm.model.FileEntity;
import ir.netpick.platform.filefarm.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "csv", "json", "xml", "png", "jpg", "jpeg", "gif", "svg",
            "zip", "rar", "mp3", "mp4", "avi", "mov", "webm"
    );

    public PageDTO<FileEntity> getAll(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<FileEntity> page = fileRepository.findByDeletedFalse(pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<FileEntity> getByFolder(UUID folderId, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<FileEntity> page = fileRepository.findByFolderIdAndDeletedFalse(folderId, pageable);
        return PageDTOMapper.map(page);
    }

    public PageDTO<FileEntity> searchByName(String name, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, GeneralConstants.PAGE_SIZE,
                Sort.by("createdAt").descending());
        Page<FileEntity> page = fileRepository.findByOriginalFileNameContainingAndDeletedFalse(name, pageable);
        return PageDTOMapper.map(page);
    }

    public FileEntity getById(UUID fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File with id [%s] was not found".formatted(fileId)));
    }

    public FileEntity upload(MultipartFile file, UUID folderId, UUID ownerId) throws IOException {
        validateFile(file);
        
        String safeFilename = sanitizeFilename(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "_" + safeFilename;
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation);

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(fileName);
        fileEntity.setOriginalFileName(safeFilename);
        fileEntity.setMimeType(file.getContentType());
        fileEntity.setFileSize(file.getSize());
        fileEntity.setFilePath(targetLocation.toString());
        fileEntity.setFolderId(folderId);
        fileEntity.setOwnerId(ownerId);
        
        return fileRepository.save(fileEntity);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("File must have a valid name");
        }
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + 
                    ALLOWED_EXTENSIONS.stream().collect(Collectors.joining(", ")));
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "unknown";
        // Remove path separators and null bytes
        String sanitized = filename.replaceAll("[/\\\\:*?\"<>|\0]", "_");
        // Remove leading dots to prevent hidden files
        sanitized = sanitized.replaceAll("^\\.", "_");
        return sanitized;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    public FileEntity update(UUID fileId, FileEntity fileDetails) {
        FileEntity existingFile = getById(fileId);
        existingFile.setOriginalFileName(fileDetails.getOriginalFileName());
        existingFile.setMimeType(fileDetails.getMimeType());
        existingFile.setFolderId(fileDetails.getFolderId());
        existingFile.setOwnerId(fileDetails.getOwnerId());
        return fileRepository.save(existingFile);
    }

    public void delete(UUID fileId) {
        fileRepository.softDelete(fileId);
    }

    public void restore(UUID fileId) {
        fileRepository.restore(fileId);
    }
}
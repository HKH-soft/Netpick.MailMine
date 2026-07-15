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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;

    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;

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
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path targetLocation = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation);

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(fileName);
        fileEntity.setOriginalFileName(file.getOriginalFilename());
        fileEntity.setMimeType(file.getContentType());
        fileEntity.setFileSize(file.getSize());
        fileEntity.setFilePath(targetLocation.toString());
        fileEntity.setFolderId(folderId);
        fileEntity.setOwnerId(ownerId);
        
        return fileRepository.save(fileEntity);
    }

    public FileEntity update(UUID fileId, FileEntity fileDetails) {
        FileEntity existingFile = getById(fileId);
        existingFile.setFileName(fileDetails.getFileName());
        existingFile.setOriginalFileName(fileDetails.getOriginalFileName());
        existingFile.setMimeType(fileDetails.getMimeType());
        existingFile.setFileSize(fileDetails.getFileSize());
        existingFile.setFilePath(fileDetails.getFilePath());
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
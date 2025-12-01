package ir.netpick.mailmine.scrape.service.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service
public class FileManagement {

    private static final Path BASE_DIR;

    static {
        // OS-specific temp directory
        String tmpDir = System.getProperty("java.io.tmpdir");
        BASE_DIR = Paths.get(tmpDir, "mailmine","files");
        try {
            Files.createDirectories(BASE_DIR);
            log.info("Scraper base directory: {}", BASE_DIR.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to initialize base directory: {}", BASE_DIR, e);
        }
    }

    public Path getFilePath(UUID id, int attemptNumber, String fileName) {
        return buildFilePath(id, attemptNumber, fileName);
    }

    private Path buildFilePath(UUID id, int attemptNumber, String fileName) {
        Path dir = BASE_DIR.resolve(id.toString()).resolve(String.valueOf(attemptNumber));
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.error("Failed to create directory: {}", dir, e);
        }
        return dir.resolve(fileName);
    }

    public void CreateAFile(UUID id, int attemptNumber, String fileName, String content) {
        Path path = buildFilePath(id, attemptNumber, fileName);
        try {
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE_NEW);
            log.info("File created: {}", path);
        } catch (FileAlreadyExistsException e) {
            log.warn("File already exists: {}", path);
        } catch (IOException e) {
            log.error("Error creating file: {}", path, e);
        }
    }

    public String ReadAFile(UUID id, int attemptNumber, String fileName) {
        Path path = buildFilePath(id, attemptNumber, fileName);
        try {
            String content = Files.readString(path);
            log.info("File read: {}", path);
            return content;
        } catch (NoSuchFileException e) {
            log.warn("File not found: {}", path);
        } catch (IOException e) {
            log.error("Error reading file: {}", path, e);
        }
        return null;
    }

    public void UpdateAFile(UUID id, int attemptNumber, String fileName, String newContent) {
        Path path = buildFilePath(id, attemptNumber, fileName);
        try {
            Files.write(path, newContent.getBytes(), StandardOpenOption.APPEND);
            log.info("File updated: {}", path);
        } catch (NoSuchFileException e) {
            log.warn("File not found: {}", path);
        } catch (IOException e) {
            log.error("Error updating file: {}", path, e);
        }
    }

    public void DeleteAFile(UUID id, int attemptNumber, String fileName) {
        Path path = buildFilePath(id, attemptNumber, fileName);
        try {
            Files.delete(path);
            log.info("File deleted: {}", path);
        } catch (NoSuchFileException e) {
            log.warn("File not found: {}", path);
        } catch (IOException e) {
            log.error("Error deleting file: {}", path, e);
        }
    }
}

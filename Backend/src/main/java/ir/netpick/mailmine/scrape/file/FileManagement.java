package ir.netpick.mailmine.scrape.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileManagement {

    private static final Logger logger = LogManager.getLogger(FileManagement.class);
    private static final Path BASE_DIR;

    static {
        // OS-specific temp directory
        String tmpDir = System.getProperty("java.io.tmpdir");
        BASE_DIR = Paths.get(tmpDir, "scraper");
        try {
            Files.createDirectories(BASE_DIR);
            logger.info("Scraper base directory: {}", BASE_DIR.toAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to initialize base directory: {}", BASE_DIR, e);
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
            logger.error("Failed to create directory: {}", dir, e);
        }
        return dir.resolve(fileName);
    }

    public void CreateAFile(UUID id, int attemptNumber, String fileName, String content) {
        Path path = buildFilePath(id, attemptNumber, fileName);
        try {
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE_NEW);
            logger.info("File created: {}", path);
        } catch (FileAlreadyExistsException e) {
            logger.warn("File already exists: {}", path);
        } catch (IOException e) {
            logger.error("Error creating file: {}", path, e);
        }
    }

    public String ReadAFile(UUID id, int attemptNumber, String fileName) {
        Path path = buildFilePath(id, attemptNumber, fileName);
        try {
            String content = Files.readString(path);
            logger.info("File read: {}", path);
            return content;
        } catch (NoSuchFileException e) {
            logger.warn("File not found: {}", path);
        } catch (IOException e) {
            logger.error("Error reading file: {}", path, e);
        }
        return null;
    }

    public void UpdateAFile(UUID id, int attemptNumber, String fileName, String newContent) {
        Path path = buildFilePath(id, attemptNumber, fileName);
        try {
            Files.write(path, newContent.getBytes(), StandardOpenOption.APPEND);
            logger.info("File updated: {}", path);
        } catch (NoSuchFileException e) {
            logger.warn("File not found: {}", path);
        } catch (IOException e) {
            logger.error("Error updating file: {}", path, e);
        }
    }

    public void DeleteAFile(UUID id, int attemptNumber, String fileName) {
        Path path = buildFilePath(id, attemptNumber, fileName);
        try {
            Files.delete(path);
            logger.info("File deleted: {}", path);
        } catch (NoSuchFileException e) {
            logger.warn("File not found: {}", path);
        } catch (IOException e) {
            logger.error("Error deleting file: {}", path, e);
        }
    }
}

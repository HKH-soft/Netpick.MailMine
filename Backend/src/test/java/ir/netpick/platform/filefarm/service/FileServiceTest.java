package ir.netpick.platform.filefarm.service;

import ir.netpick.platform.core.PageDTO;
import ir.netpick.platform.core.exception.ResourceNotFoundException;
import ir.netpick.platform.filefarm.model.FileEntity;
import ir.netpick.platform.filefarm.repository.FileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @InjectMocks
    private FileService fileService;

    private UUID testFileId;
    private UUID testFolderId;
    private UUID testOwnerId;
    private FileEntity testFile;

    @BeforeEach
    void setUp() {
        testFileId = UUID.randomUUID();
        testFolderId = UUID.randomUUID();
        testOwnerId = UUID.randomUUID();

        testFile = new FileEntity();
        testFile.setId(testFileId);
        testFile.setOriginalFileName("test.pdf");
        testFile.setMimeType("application/pdf");
    }

    @Nested
    @DisplayName("getAll Tests")
    class GetAllTests {
        @Test
        @DisplayName("Should return paginated files sorted by createdAt descending")
        void shouldReturnPaginatedFiles() {
            Page<FileEntity> mockPage = new PageImpl<>(List.of(testFile));
            when(fileRepository.findByDeletedFalse(any(PageRequest.class))).thenReturn(mockPage);

            PageDTO<FileEntity> result = fileService.getAll(1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
        }
    }

    @Nested
    @DisplayName("getByFolder Tests")
    class GetByFolderTests {
        @Test
        @DisplayName("Should return files filtered by folder")
        void shouldReturnFilesByFolder() {
            Page<FileEntity> mockPage = new PageImpl<>(List.of(testFile));
            when(fileRepository.findByFolderIdAndDeletedFalse(eq(testFolderId), any(PageRequest.class)))
                    .thenReturn(mockPage);

            PageDTO<FileEntity> result = fileService.getByFolder(testFolderId, 1);

            assertNotNull(result);
            assertEquals(1, result.content().size());
        }
    }

    @Nested
    @DisplayName("searchByName Tests")
    class SearchByNameTests {
        @Test
        @DisplayName("Should search files by name")
        void shouldSearchByName() {
            Page<FileEntity> mockPage = new PageImpl<>(List.of(testFile));
            when(fileRepository.findByOriginalFileNameContainingAndDeletedFalse(eq("test"), any(PageRequest.class)))
                    .thenReturn(mockPage);

            PageDTO<FileEntity> result = fileService.searchByName("test", 1);

            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("getById Tests")
    class GetByIdTests {
        @Test
        @DisplayName("Should return file when found")
        void shouldReturnFileWhenFound() {
            when(fileRepository.findById(testFileId)).thenReturn(Optional.of(testFile));

            FileEntity result = fileService.getById(testFileId);

            assertNotNull(result);
            assertEquals(testFileId, result.getId());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(fileRepository.findById(testFileId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> fileService.getById(testFileId));
        }
    }

    @Nested
    @DisplayName("upload Tests")
    class UploadTests {
        @Test
        @DisplayName("Should throw for empty file")
        void shouldThrowForEmptyFile() {
            MultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

            assertThrows(IllegalArgumentException.class, 
                    () -> fileService.upload(emptyFile, testFolderId, testOwnerId));
        }

        @Test
        @DisplayName("Should throw for null filename")
        void shouldThrowForNullFilename() {
            MultipartFile fileNoName = new MockMultipartFile("file", null, "text/plain", "content".getBytes());

            assertThrows(IllegalArgumentException.class, 
                    () -> fileService.upload(fileNoName, testFolderId, testOwnerId));
        }

        @Test
        @DisplayName("Should throw for invalid file extension")
        void shouldThrowForInvalidExtension() {
            byte[] content = "test content".getBytes();
            MultipartFile invalidFile = new MockMultipartFile("file", "test.xyz", "application/octet-stream", content);

            assertThrows(IllegalArgumentException.class, 
                    () -> fileService.upload(invalidFile, testFolderId, testOwnerId));
        }
    }

    @Nested
    @DisplayName("update Tests")
    class UpdateTests {
        @Test
        @DisplayName("Should update file metadata")
        void shouldUpdateFile() {
            FileEntity updateDetails = new FileEntity();
            updateDetails.setOriginalFileName("updated.pdf");

            when(fileRepository.findById(testFileId)).thenReturn(Optional.of(testFile));
            when(fileRepository.save(any(FileEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            FileEntity result = fileService.update(testFileId, updateDetails);

            assertEquals("updated.pdf", result.getOriginalFileName());
        }

        @Test
        @DisplayName("Should throw when file not found for update")
        void shouldThrowWhenUpdatingNonExistent() {
            when(fileRepository.findById(testFileId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> fileService.update(testFileId, new FileEntity()));
        }
    }

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {
        @Test
        @DisplayName("Should soft delete file")
        void shouldSoftDelete() {
            doNothing().when(fileRepository).softDelete(testFileId);

            fileService.delete(testFileId);

            verify(fileRepository).softDelete(testFileId);
        }
    }

    @Nested
    @DisplayName("restore Tests")
    class RestoreTests {
        @Test
        @DisplayName("Should restore soft deleted file")
        void shouldRestore() {
            doNothing().when(fileRepository).restore(testFileId);

            fileService.restore(testFileId);

            verify(fileRepository).restore(testFileId);
        }
    }

    @Nested
    @DisplayName("sanitizeFilename Tests")
    class SanitizeFilenameTests {
        @Test
        @DisplayName("Should sanitize path traversal characters")
        void shouldSanitizePathTraversal() {
            MultipartFile maliciousFile = new MockMultipartFile("file", "../../../etc/passwd", "text/plain", "content".getBytes());

            assertThrows(IllegalArgumentException.class, 
                    () -> fileService.upload(maliciousFile, testFolderId, testOwnerId));
        }
    }
}
package ir.netpick.platform.filefarm.repository;

import ir.netpick.platform.filefarm.model.FileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    // Find all non-deleted files with pagination
    Page<FileEntity> findByDeletedFalse(Pageable pageable);

    // Find files by folder with pagination
    Page<FileEntity> findByFolderIdAndDeletedFalse(UUID folderId, Pageable pageable);

    // Find files by owner with pagination
    Page<FileEntity> findByOwnerIdAndDeletedFalse(UUID ownerId, Pageable pageable);

    // Search by original file name
    Page<FileEntity> findByOriginalFileNameContainingAndDeletedFalse(String name, Pageable pageable);

    // Soft delete
    @Transactional
    @Modifying
    @Query("update FileEntity f set f.deleted = true where f.deleted = false and f.id = ?1")
    void softDelete(UUID id);

    // Restore
    @Transactional
    @Modifying
    @Query("update FileEntity f set f.deleted = false where f.id = ?1 and f.deleted = true")
    void restore(UUID id);
}
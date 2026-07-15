package ir.netpick.platform.filefarm.repository;

import ir.netpick.platform.filefarm.model.Folder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID> {

    // Find all non-deleted folders with pagination
    Page<Folder> findByDeletedFalse(Pageable pageable);

    // Find folders by parent with pagination
    Page<Folder> findByParentIdAndDeletedFalse(UUID parentId, Pageable pageable);

    // Find folders by owner
    List<Folder> findByOwnerIdAndDeletedFalse(UUID ownerId);

    // Soft delete
    @Transactional
    @Modifying
    @Query("update Folder f set f.deleted = true where f.deleted = false and f.id = ?1")
    void softDelete(UUID id);

    // Restore
    @Transactional
    @Modifying
    @Query("update Folder f set f.deleted = false where f.id = ?1 and f.deleted = true")
    void restore(UUID id);
}
package ir.netpick.platform.taskfarm.repository;

import ir.netpick.platform.taskfarm.model.Project;
import ir.netpick.platform.taskfarm.model.Project.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // Find all non-deleted projects with pagination
    Page<Project> findByDeletedFalse(Pageable pageable);

    // Find projects by owner with pagination
    Page<Project> findByOwnerIdAndDeletedFalse(UUID ownerId, Pageable pageable);

    // Count tasks for a project
    @Query("select count(t) from Task t where t.projectId = ?1 and t.deleted = false")
    long countTasksByProjectId(UUID projectId);

    // Soft delete
    @Transactional
    @Modifying
    @Query("update Project p set p.deleted = true where p.deleted = false and p.id = ?1")
    void softDelete(UUID id);

    // Restore
    @Transactional
    @Modifying
    @Query("update Project p set p.deleted = false where p.id = ?1 and p.deleted = true")
    void restore(UUID id);
}
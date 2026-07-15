package ir.netpick.platform.taskfarm.repository;

import ir.netpick.platform.taskfarm.model.Task;
import ir.netpick.platform.taskfarm.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Find all non-deleted tasks with pagination
    Page<Task> findByDeletedFalse(Pageable pageable);

    // Find tasks by status with pagination
    Page<Task> findByStatusAndDeletedFalse(TaskStatus status, Pageable pageable);

    // Find tasks by assignee with pagination
    Page<Task> findByAssigneeIdAndDeletedFalse(UUID assigneeId, Pageable pageable);

    // Find tasks by project with pagination
    Page<Task> findByProjectIdAndDeletedFalse(UUID projectId, Pageable pageable);

    // Find tasks by due date range
    Page<Task> findByDueDateBetweenAndDeletedFalse(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Soft delete
    @Transactional
    @Modifying
    @Query("update Task t set t.deleted = true where t.deleted = false and t.id = ?1")
    void softDelete(UUID id);

    // Restore
    @Transactional
    @Modifying
    @Query("update Task t set t.deleted = false where t.id = ?1 and t.deleted = true")
    void restore(UUID id);
}
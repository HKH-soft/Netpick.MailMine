package ir.netpick.platform.taskfarm.repository;

import ir.netpick.platform.taskfarm.model.TaskLabel;
import ir.netpick.platform.taskfarm.model.TaskLabel.TaskLabelId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskLabelRepository extends JpaRepository<TaskLabel, TaskLabelId> {

    List<TaskLabel> findByTaskId(UUID taskId);

    List<TaskLabel> findByLabelId(UUID labelId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TaskLabel tl WHERE tl.id.taskId = ?1")
    void deleteByTaskId(UUID taskId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TaskLabel tl WHERE tl.id.labelId = ?1")
    void deleteByLabelId(UUID labelId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TaskLabel tl WHERE tl.id.taskId = ?1 AND tl.id.labelId = ?2")
    void deleteByTaskIdAndLabelId(UUID taskId, UUID labelId);
}
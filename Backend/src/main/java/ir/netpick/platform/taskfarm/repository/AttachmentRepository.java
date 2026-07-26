package ir.netpick.platform.taskfarm.repository;

import ir.netpick.platform.taskfarm.model.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByTaskIdAndDeletedFalse(UUID taskId);

    @Transactional
    @Modifying
    @Query("UPDATE Attachment a SET a.deleted = true WHERE a.id = :id")
    void softDelete(UUID id);
}
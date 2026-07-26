package ir.netpick.platform.taskfarm.repository;

import ir.netpick.platform.taskfarm.model.Comment;
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
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByTaskIdAndDeletedFalseOrderByCreatedAtAsc(UUID taskId);

    Page<Comment> findByDeletedFalse(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.deleted = true WHERE c.id = :id")
    void softDelete(UUID id);
}
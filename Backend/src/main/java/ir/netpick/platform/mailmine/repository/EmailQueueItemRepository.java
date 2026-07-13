package ir.netpick.platform.mailmine.repository;

import ir.netpick.platform.mailmine.model.EmailQueueItem;
import ir.netpick.platform.mailmine.model.EmailQueueItem.QueueStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailQueueItemRepository extends JpaRepository<EmailQueueItem, UUID> {

    @Query("SELECT e FROM EmailQueueItem e WHERE e.status = :status ORDER BY e.createdAt ASC")
    List<EmailQueueItem> findTopNByStatusOrderByCreatedAtAsc(
            @Param("status") QueueStatus status, 
            Pageable pageable);

    List<EmailQueueItem> findByCreatedByUserId(UUID userId);

    List<EmailQueueItem> findByStatusAndRetryCountLessThan(QueueStatus status, int maxRetries);
}









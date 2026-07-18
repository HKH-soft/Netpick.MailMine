package ir.netpick.platform.taskfarm.repository;

import ir.netpick.platform.taskfarm.model.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LabelRepository extends JpaRepository<Label, UUID> {

    List<Label> findByProjectIdAndDeletedFalse(UUID projectId);

    Page<Label> findByDeletedFalse(Pageable pageable);

    @Modifying
    @Query("UPDATE Label l SET l.deleted = true WHERE l.id = :id")
    void softDelete(UUID id);

    @Modifying
    @Query("UPDATE Label l SET l.deleted = false WHERE l.id = :id")
    void restore(UUID id);
}
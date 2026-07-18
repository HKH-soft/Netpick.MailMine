package ir.netpick.platform.taskfarm.repository;

import ir.netpick.platform.taskfarm.model.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LabelRepository extends JpaRepository<Label, UUID> {

    List<Label> findByProjectIdAndDeletedFalse(UUID projectId);

    Page<Label> findByDeletedFalse(Pageable pageable);
}
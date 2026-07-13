package ir.netpick.platform.mailmine.repository;

import ir.netpick.platform.mailmine.model.EmailTagAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailTagAssignmentRepository extends JpaRepository<EmailTagAssignment, UUID> {

    List<EmailTagAssignment> findByEmailMessageId(UUID emailMessageId);

    List<EmailTagAssignment> findByEmailTagId(UUID tagId);

    void deleteByEmailMessageId(UUID emailMessageId);
}









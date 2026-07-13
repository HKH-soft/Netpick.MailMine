package ir.netpick.platform.mailmine.repository;

import ir.netpick.platform.mailmine.model.EmailRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailRuleRepository extends JpaRepository<EmailRule, UUID> {

    List<EmailRule> findByIsActiveTrueOrderByPriorityDesc();

    List<EmailRule> findByCreatedById(UUID userId);
}









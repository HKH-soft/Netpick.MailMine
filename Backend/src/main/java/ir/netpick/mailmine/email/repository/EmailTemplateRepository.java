package ir.netpick.mailmine.email.repository;

import ir.netpick.mailmine.email.model.EmailTemplate;
import ir.netpick.mailmine.email.model.EmailTemplate.TemplateCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {

    Optional<EmailTemplate> findByName(String name);

    List<EmailTemplate> findByCategory(TemplateCategory category);

    List<EmailTemplate> findByIsSharedTrue();

    List<EmailTemplate> findByCreatedById(UUID userId);
}
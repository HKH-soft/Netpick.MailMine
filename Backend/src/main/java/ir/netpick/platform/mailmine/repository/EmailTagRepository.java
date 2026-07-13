package ir.netpick.platform.mailmine.repository;

import ir.netpick.platform.mailmine.model.EmailTag;
import ir.netpick.platform.mailmine.model.EmailTag.TagCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailTagRepository extends JpaRepository<EmailTag, UUID> {

    Optional<EmailTag> findByName(String name);

    List<EmailTag> findByCategory(TagCategory category);

    List<EmailTag> findByDeletedFalse();

    List<EmailTag> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}









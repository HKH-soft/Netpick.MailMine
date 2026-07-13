package ir.netpick.platform.mailmine.repository;

import ir.netpick.platform.mailmine.model.SharedInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SharedInboxRepository extends JpaRepository<SharedInbox, UUID> {

    Optional<SharedInbox> findByEmailAddress(String emailAddress);

    List<SharedInbox> findByIsActiveTrue();

    List<SharedInbox> findByMembersId(UUID userId);

    List<SharedInbox> findByNameContainingIgnoreCaseOrEmailAddressContainingIgnoreCase(String name, String email);
}









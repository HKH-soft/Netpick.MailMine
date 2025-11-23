package ir.netpick.mailmine.scrape.repository;

import ir.netpick.mailmine.scrape.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface ContactRepository extends JpaRepository<Contact, UUID> {

    @Transactional
    @Modifying
    @Query("update Contact c set c.deleted = True where c.deleted = false and c.id = ?1")
    void softDelete(UUID id);

    @Transactional
    @Modifying
    @Query("update Contact c set c.deleted = False where c.id = ?1 and c.deleted = true")
    void restore(UUID id);
}

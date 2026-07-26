package ir.netpick.platform.financefarm.repository;

import ir.netpick.platform.financefarm.model.CustomsDeclaration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomsDeclarationRepository extends JpaRepository<CustomsDeclaration, UUID> {
    Optional<CustomsDeclaration> findByDeclarationNumber(String declarationNumber);
    Page<CustomsDeclaration> findByDeletedFalse(Pageable pageable);
    Page<CustomsDeclaration> findByStatusAndDeletedFalse(String status, Pageable pageable);
    Page<CustomsDeclaration> findByCreatedByAndDeletedFalse(UUID createdBy, Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE CustomsDeclaration c SET c.deleted = true WHERE c.id = :id")
    void softDelete(UUID id);
}
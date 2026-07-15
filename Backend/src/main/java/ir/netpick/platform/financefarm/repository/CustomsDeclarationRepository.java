package ir.netpick.platform.financefarm.repository;

import ir.netpick.platform.financefarm.model.CustomsDeclaration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomsDeclarationRepository extends JpaRepository<CustomsDeclaration, UUID> {
    Optional<CustomsDeclaration> findByDeclarationNumber(String declarationNumber);
    Page<CustomsDeclaration> findByDeletedFalse(Pageable pageable);
    Page<CustomsDeclaration> findByStatusAndDeletedFalse(String status, Pageable pageable);
    Page<CustomsDeclaration> findByCreatedByIdAndDeletedFalse(UUID createdBy, Pageable pageable);
}
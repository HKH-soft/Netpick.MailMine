package ir.netpick.mailmine.auth.repository;

import ir.netpick.mailmine.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // ==================== Find Operations ====================

    Optional<User> findByEmail(String email);

    Optional<User> findByDeletedFalseAndEmail(String email);

    Optional<User> findByDeletedFalseAndId(UUID id);

    // ==================== Exists Operations ====================

    boolean existsUserByEmail(String email);

    boolean existsUserByDeletedFalseAndEmail(String email);

    // ==================== Pagination ====================

    Page<User> findByDeletedFalse(Pageable pageable);

    Page<User> findByDeletedTrue(Pageable pageable);

    // ==================== Update Operations ====================

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.lastLoginAt = :now WHERE u.email = :email")
    void updateLastLogin(@Param("now") LocalDateTime now, @Param("email") String email);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.deleted = :deleted WHERE u.email = :email")
    int updateDeletedByEmail(@Param("deleted") Boolean deleted, @Param("email") String email);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.deleted = :deleted WHERE u.id = :id")
    int updateDeletedById(@Param("deleted") Boolean deleted, @Param("id") UUID id);

    // ==================== Delete Operations ====================

    @Transactional
    void deleteByEmail(String email);
}

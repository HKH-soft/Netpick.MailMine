package ir.netpick.mailmine.auth.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ir.netpick.mailmine.auth.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsUserByEmail(String email);

    boolean existsUserByDeletedFalseAndEmail(String email);

    @Modifying()
    @Query("UPDATE User u SET u.lastLoginAt = :now WHERE u.email = :email")
    void updateLastLogin(@Param("now") LocalDateTime now, @Param("email") String email);

    void deleteByEmail(String email);

    @Transactional
    @Modifying
    @Query("update User u set u.deleted = ?1 where u.email = ?2")
    int updateDeletedByEmail(Boolean deleted, String email);

    Page<User> findByDeletedFalse(Pageable pageable);

    Optional<User> findByDeletedFalseAndEmail(String email);
}

package org.example.userservice.repository;

import org.example.userservice.model.PasswordResetToken;
import org.example.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken t SET t.valid = false WHERE t.user = :user AND t.used = false AND t.valid = true")
    void invalidateAllValidTokensForUser(@Param("user") User user);
}

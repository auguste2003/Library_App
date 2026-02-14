package com.library.auth.repository;

import com.library.auth.entity.PasswordResetToken;
import com.library.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    void deleteByToken(String token);

    void deleteByUser(User user); // Careful with this one
}

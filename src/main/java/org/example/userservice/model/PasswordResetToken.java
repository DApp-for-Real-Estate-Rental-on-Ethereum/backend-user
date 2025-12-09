package org.example.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "is_used", nullable = false)
    private boolean used = false;

    @Column(name = "is_valid", nullable = false)
    private boolean valid = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}

package org.example.userservice.dto.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserStatsDTO {
    private Long id;
    private Double rating;
    private Integer score;
    private LocalDateTime createdAt;
    private boolean isVerified; // Mapped from 'enabled'
}

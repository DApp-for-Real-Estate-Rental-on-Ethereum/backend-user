package org.example.userservice.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequestDTO {
    private String userId;
    private Boolean complete;
}


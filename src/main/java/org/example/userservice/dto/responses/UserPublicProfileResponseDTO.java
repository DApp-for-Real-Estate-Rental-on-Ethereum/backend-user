package org.example.userservice.dto.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserPublicProfileResponseDTO {
    private String firstName;
    private String lastName;
    private String profilePicture;
    private Long phoneNumber;
}

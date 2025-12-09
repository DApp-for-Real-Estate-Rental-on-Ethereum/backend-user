package org.example.userservice.dto.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.userservice.enums.UserRoleEnum;

import java.time.LocalDate;
import java.util.Set;

@Builder
@AllArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.ALWAYS)
public class UserMeResponseDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
    private LocalDate birthday;
    private Long phoneNumber;
    
    @JsonProperty("walletAddress")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String walletAddress;
    
    @JsonProperty("roles")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Set<UserRoleEnum> roles;
    
    @JsonProperty("score")
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Integer score;
}

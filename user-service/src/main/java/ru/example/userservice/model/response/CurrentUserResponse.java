package ru.example.userservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserResponse {

    private String accountPublicId;
    private String keycloakUserId;
    private String email;
    private String firstName;
    private String lastName;
    private String accountType;
    private String accountStatus;
}
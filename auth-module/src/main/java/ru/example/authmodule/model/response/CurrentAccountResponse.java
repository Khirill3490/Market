package ru.example.authmodule.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentAccountResponse {

    private String keycloakUserId;
    private String email;
    private String firstName;
    private String lastName;

    private boolean localAccountExists;
    private String accountPublicId;
    private String accountType;
    private String accountStatus;
}
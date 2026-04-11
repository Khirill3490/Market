package ru.example.authmodule.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentAccountResponse {

    private String keycloakUserId;
    private String username;
    private String email;
    private List<String> authorities;

    private boolean localAccountExists;
    private String localPublicId;
    private String accountType;
    private String status;
    private String companyPublicId;
}
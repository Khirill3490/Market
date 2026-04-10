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
public class CurrentUserResponse {

    private String keycloakUserId;
    private String username;
    private String email;
    private List<String> authorities;

    private boolean localUserExists;
    private String localPublicId;
    private String localRole;
    private Boolean localActive;
}
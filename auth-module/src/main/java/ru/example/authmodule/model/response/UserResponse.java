package ru.example.authmodule.model.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.example.identitydomain.entity.enums.RoleType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String email;

    private String password;

    private List<RoleType> roles;

}

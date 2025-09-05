package ru.example.authmodule.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegRequest {
//    TODO validation
    private String email;
    private String password;
    private String confirmPassword;
    private String companyName;
    private String inn;
    private String city;
    private String phone;
    private String mail;
}

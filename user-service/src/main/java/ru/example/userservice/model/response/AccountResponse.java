package ru.example.userservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponse {

    private String username;
    private boolean isActive;
    private String companyName;
    private String inn;
    private String city;
    private String phone;
    private String mail;
    private String url;
    private String rules;

    private String logo;
    private Double tax;
    private Double rating;
}

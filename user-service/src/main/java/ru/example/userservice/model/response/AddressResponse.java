package ru.example.userservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private String publicId;
    private String label;
    private String country;
    private String region;
    private String city;
    private String street;
    private String house;
    private String apartment;
    private String postalCode;
    private String comment;
    private boolean isDefault;
}
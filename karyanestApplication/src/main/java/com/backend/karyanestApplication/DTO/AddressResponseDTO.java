package com.backend.karyanestApplication.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddressResponseDTO {
    private Long id;
    private String nearbyLandmarks;
    private String locationAddress;
    private String city;
    private String area;
    private String district;
    private String state;
    private String country;
    private String pincode;
    private Double latitude;
    private Double longitude;

}

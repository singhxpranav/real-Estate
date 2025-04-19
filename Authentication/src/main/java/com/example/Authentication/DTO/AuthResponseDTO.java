package com.example.Authentication.DTO;

//import com.backend.karyanestApplication.DTO.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String jwtToken;
    private String refreshToken;
    private String role;
//    private UserDTO userDetails;
    private List<Map<String, Object>> permissions;
}














































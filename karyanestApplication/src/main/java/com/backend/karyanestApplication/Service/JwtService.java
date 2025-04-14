package com.backend.karyanestApplication.Service;

import com.backend.karyanestApplication.Controller.AuthController;
import com.backend.karyanestApplication.DTO.AuthResponseDTO;
import com.backend.karyanestApplication.DTO.UserResponseDTO;
import com.backend.karyanestApplication.Exception.CustomException;
import com.backend.karyanestApplication.JwtSecurity.JwtUtil;
import com.backend.karyanestApplication.Model.RolePermission;
import com.backend.karyanestApplication.Model.User;
import com.backend.karyanestApplication.Model.UserRole;
import com.backend.karyanestApplication.Repositry.RolePermissionRepository;
import com.backend.karyanestApplication.Repositry.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ReferenceTokenService referenceTokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private UserRepo userRepo;
    /**
     * Generate authentication response for a user
     *
     * @param user the user to generate authentication for
     * @return ResponseEntity with auth response or error
     */
    public ResponseEntity<?> generateAuthResponseForUser(User user) {
        // Check if user is verified
        if (user.getVerificationStatus() == User.VerificationStatus.Unverified) {
                return userService.notifyUser(user, UserService.Phase.LOGIN);
        }
        // Generate JWT token
        UserRole userRole = user.getUserRole();
        String jwtToken = jwtUtil.generateToken(user.getUsername(), userRole.getName(), user.getId(),user.getFullName());
        String refreshToken = referenceTokenService.generateReferenceToken(jwtToken);

        // Update user status
        user.setStatus(User.UserStatus.Active);
        userService.updateUserStatus(user);

        // Generate and return response
        AuthResponseDTO authResponse = getJwtResponse(jwtToken, refreshToken, user,userRole);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Create JWT response with user details and permissions.
     *
     * @param jwtToken the JWT token
     * @param refreshToken the refresh token
     * @param user the user
     * @return the authentication response DTO
     * @throws CustomException if user or role not found
     */
    public AuthResponseDTO getJwtResponse(String jwtToken, String refreshToken, User user,UserRole userRole) {
        // Fetch role permissions
        List<RolePermission> rolePermissions = rolePermissionRepository.findPermissionsByRoleId(userRole.getId());

        // Group permissions by route path
        List<Map<String, Object>> permissions = rolePermissions.stream()
                .collect(Collectors.groupingBy(
                        rp -> rp.getRoute().getPath(),
                        Collectors.mapping(rp -> rp.getPermission().getName(), Collectors.toList())
                ))
                .entrySet()
                .stream()
                .map(entry -> Map.of("path", entry.getKey(), "actions", entry.getValue()))
                .toList();

        // Fetch user response DTO
        UserResponseDTO userResponseDTO = userService.mapToDTO(user);

        // Return JWT response
        return new AuthResponseDTO(jwtToken, refreshToken, userRole.getName(), userResponseDTO, permissions);
    }
}

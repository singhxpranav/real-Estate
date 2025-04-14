package com.backend.karyanestApplication.Controller;

import com.backend.karyanestApplication.DTO.AuthResponseDTO;
import com.backend.karyanestApplication.DTO.UserPreferencesDTO;
import com.backend.karyanestApplication.DTO.UserRegistrationDTO;
import com.backend.karyanestApplication.DTO.UserResponseDTO;
import com.backend.karyanestApplication.Model.User;
import com.backend.karyanestApplication.Service.JwtService;
import com.backend.karyanestApplication.Service.UserService;
import com.backend.karyanestApplication.UTIL.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for account-related operations.
 */
@RestController
@RequestMapping("/v1/users")
@Validated
@Tag(name = "User", description = "User operations")
public class AccountController {

    private final UserService userService;
   private final UserContext userContext;
   private final JwtService jwtService;
    private static final Logger logger = LoggerFactory.getLogger(LeadsController.class);

    enum LoginMethod {
        USERNAME, EMAIL, PHONE
    }
    public AccountController(UserService userService, UserContext userContext, JwtService jwtService, AuthController authController) {
        this.userService = userService;
        this.userContext = userContext;
        this.jwtService = jwtService;
    }
    /**
     * Get all users.
     *
     * @return ResponseEntity with a list of all users
     */
    @Operation(summary = "Get all users", description = "Retrieve a list of all registered users")
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get a user by ID.
     *
     * @param id The ID of the user
     * @return ResponseEntity with user details
     */
    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID")
    @GetMapping("{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
       UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/currentUser")
    public ResponseEntity<?> getUserDetails(HttpServletRequest request) {
        String username = userContext.getUsername(request);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        User user = userService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        // ✅ Required User Details mapped to DTO
        UserResponseDTO userDTO = userService.mapToDTO(user);

        // ✅ JWT Response
        ResponseEntity<?> jwtResponseEntity = jwtService.generateAuthResponseForUser(user);

        // ✅ JWT Response ki body extract karein
        Object jwtBody = jwtResponseEntity.getBody();

        // ✅ Ensure karein ki body `AuthResponseDTO` ka object hai
        if (!(jwtBody instanceof AuthResponseDTO authResponse)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Invalid response format from JWT service"));
        }

        // ✅ JWT Response ko Map me convert karein aur User DTO add karein
        // ✅ LinkedHashMap for ordered entries
        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("user", userDTO);            // First item
        responseBody.put("jwtToken", authResponse.getJwtToken());
        responseBody.put("refreshToken", authResponse.getRefreshToken());
        responseBody.put("role", authResponse.getRole());
        responseBody.put("permissions", authResponse.getPermissions());

      // ✅ Final Response return karein
        return ResponseEntity.ok(responseBody);
    }
    /**
     * Update user details.
     *
     * @param id The ID of the user
     * @param userDTO The updated user details
     * @return ResponseEntity with updated user details
     */
    @Operation(summary = "Update user details", description = "Update details of an existing user by user ID")
    @PutMapping("{id}")
    public ResponseEntity<UserRegistrationDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UserRegistrationDTO userDTO) {

        User updatedUser = userService.updateUser(id, userDTO);

        UserRegistrationDTO responseDTO = new UserRegistrationDTO(
                updatedUser.getFullName(),
                updatedUser.getEmail(),
                updatedUser.getPhoneNumber(),
                null,
                updatedUser.getProfilePicture(),
                updatedUser.getAddress(),
                updatedUser.getCity(),
                updatedUser.getState(),
                updatedUser.getCountry(),
                updatedUser.getPincode(),
                updatedUser.getPreferences(),
                updatedUser.getCreatedAt(),
                updatedUser.getUpdatedAt(),
                updatedUser.getVerificationMethod().name()
        );

        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Delete (deactivate) a user by ID.
     *
     * @param id The ID of the user
     * @return ResponseEntity with a deactivation message
     */
    @Operation(summary = "Delete (deactivate) user by ID", description = "Deactivate a user by user ID")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user != null) {
            user.setStatus(User.UserStatus.Deleted);
            userService.updateUserStatus(user);
            return ResponseEntity.ok("User successfully deactivated.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }
    /**
     * Handles user activation or updates user preferences based on the provided input.
     *
     * @param id The ID of the user to activate or update preferences.
     * @param action Optional query parameter to specify the action (e.g., "activate").
     * @param userPreferencesDTO Optional request body containing the user preferences to update.
     * @return ResponseEntity containing a success message or updated user data, or an error message if the request fails.
     */
    @Operation(summary = "Activate user or update preferences", description = "Activate a previously deactivated user by ID or update user preferences.")
    @PatchMapping("/{id}")
    public ResponseEntity<?> activateUser(
            @PathVariable Long id,
            @RequestParam(required = false) String action,
            @RequestBody(required = false) UserPreferencesDTO userPreferencesDTO) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        // Activate User
        if ("activate".equalsIgnoreCase(action)) {
            if (user.getStatus() == User.UserStatus.Deleted) {
                user.setStatus(User.UserStatus.Active);
                userService.updateUserStatus(user);
                return ResponseEntity.ok("User successfully activated.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User is already active.");
            }
        }
        // Update Preferences
        if (userPreferencesDTO != null) {
            UserResponseDTO userResponseDTO = userService.updateUserPreferences(id, userPreferencesDTO);
            return ResponseEntity.ok(userResponseDTO);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request. Provide a valid action or preferences.");
    }
}


package com.backend.karyanestApplication.Service;
import com.backend.karyanestApplication.DTO.UserPreferencesDTO;
import com.backend.karyanestApplication.DTO.UserRegistrationDTO;
import com.backend.karyanestApplication.DTO.UserResponseDTO;
import com.backend.karyanestApplication.Exception.CustomException;
import com.backend.karyanestApplication.Model.PasswordResetToken;
import com.backend.karyanestApplication.Model.User;
import com.backend.karyanestApplication.Model.UserRole;
import com.backend.karyanestApplication.Repositry.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    public enum Phase
    {
        LOGIN,REGISTRATION
    }
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private  PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PropertyFavoriteRepository propertyFavoriteRepository;

    // Configuration constants
    private static final String VERIFICATION_LINK_TEMPLATE = "http://karynest-real-state.azurewebsites.net/v1/auth/verify?email=";

    // In-memory OTP storage
    private final Map<String, String> otpStorage = new HashMap<>();

    // In-memory registration OTP storage (separate from login OTP)
    private final Map<String, String> registrationOtpStorage = new HashMap<>();

//    /**
//     * Register a new user in the system.
//     *
//     * @param userDTO the user registration data
//     * @return the registered User entity
//     * @throws CustomException if registration fails
//     */
    /**
     * Register a new user in the system.
     *
     * @param userDTO the user registration data
     * @return the registered User entity
     * @throws Exception if registration fails
     */
    @Transactional
    public User register(@Valid UserRegistrationDTO userDTO,Long parent_id) throws Exception {
        // Validate input

        validateUserRegistrationInput(userDTO);

        // Check for existing email or phone number
        checkExistingUser(userDTO);

        // Map DTO to User entity
        User user = mapToEntity(userDTO);


        // Check if password is provided, otherwise keep the old password
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        if(parent_id!=null) {

            user.setParentCode(parent_id);
            String refer_code=referCodeGenerater();
            user.setReferCode(refer_code);
        }
        // Set verification details
        setVerificationDetails(user, userDTO);

        // Set username
        user.setUsername(generateUniqueUsername(userDTO.getFullName()));

        // Set default role
        setDefaultUserRole(user);

        // Save and return the user
        return userRepo.save(user);
    }

    /**
     * Validate user registration input.
     *
     * @param userDTO the user registration data
     * @throws CustomException if validation fails
     */
    private void validateUserRegistrationInput(UserRegistrationDTO userDTO) {

        // Validate email if provided
        // Encode password
        if (userDTO.getPassword() == null || userDTO.getPassword().isEmpty()) {
            throw new CustomException("Password cannot be null or empty");
        }

        if (userDTO.getEmail() == null && userDTO.getPhoneNumber() == null) {
            throw new CustomException("Either email or phone number must be provided.");
        }

        // Validate preferred verification method
        if (userDTO.getPreferredVerificationMethod() == null) {
            throw new CustomException("Preferred verification method must be specified.");
        }

        // Additional validations can be added here (e.g., email format, phone number format)
    }

    /**
     * Check if email or phone number is already registered.
     *
     * @param userDTO the user registration data
     * @throws CustomException if email or phone number is already in use
     */
    private void checkExistingUser(UserRegistrationDTO userDTO) throws Exception {
        if (userDTO.getEmail() != null && userRepo.existsByemail(userDTO.getEmail())) {
            throw new Exception("Email is already registered.");
        }

        if (userDTO.getPhoneNumber() != null && userRepo.existsByPhoneNumber(userDTO.getPhoneNumber())) {
            throw new Exception("Phone number is already registered.");
        }
    }

    /**
     * Set verification details for the user.
     *
     * @param user the user entity
     * @param userDTO the user registration data
     */
    private void setVerificationDetails(User user, UserRegistrationDTO userDTO) {
        // Set initial verification status
        user.setVerificationStatus(User.VerificationStatus.Unverified);

        // Determine verification method
        User.VerificationMethod verificationMethod = determineVerificationMethod(userDTO);
        user.setVerificationMethod(verificationMethod);

        // Set contact information
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
    }

    /**
     * Determine the verification method based on user preference and available contact methods.
     *
     * @param userDTO the user registration data
     * @return the verification method
     * @throws CustomException if verification method cannot be determined
     */
    private User.VerificationMethod determineVerificationMethod(UserRegistrationDTO userDTO) {
        String preferredMethod = userDTO.getPreferredVerificationMethod().toUpperCase();

        return switch (preferredMethod) {
            case "EMAIL" -> {
                if (userDTO.getEmail() == null) {
                    throw new CustomException("Email verification selected but no email provided.");
                }
                yield User.VerificationMethod.Email;
            }
            case "PHONE" -> {
                if (userDTO.getPhoneNumber() == null) {
                    throw new CustomException("Phone verification selected but no phone number provided.");
                }
                yield User.VerificationMethod.Phone;
            }
            default -> throw new CustomException("Invalid verification method.");
        };
    }

    /**
     * Set the default user role.
     *
     * @param user the user entity
     * @throws CustomException if default role cannot be found
     */
    private void setDefaultUserRole(User user) {
        UserRole defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new CustomException("Default role not found!"));
        user.setUserRole(defaultRole);
    }
    /**
     * Generate and store OTP for login
     *
     * @param phoneNumber the user's phone number
     * @return the generated OTP
     */
    public String generateAndStoreOtp(String phoneNumber) {
        // For simplicity, using "123" as in original code
        // In production, use a secure random generator
        String otp = "123456";
        otpStorage.put(phoneNumber, otp);
        registrationOtpStorage.put(phoneNumber,otp);
        return otp;
    }

    /**
     * Send verification email to user.
     *
     * @param email the user's email address
     * @param token the verification token
     * @throws CustomException if email sending fails
     */
    public void sendVerificationEmail(String email, String token) {
        try {
            String verificationLink = VERIFICATION_LINK_TEMPLATE + "?email=" + email;
            String emailBody = "Click the link to verify your account: <a href=\"" + verificationLink + "\">Verify Now</a>"
                    + "<br><br><strong>Note:</strong> Your verification token is <b>" + token + "</b>. "
                    + "Without this token, verification will not be completed.";

            emailService.sendVerificationEmail(email, "Verify your email", emailBody);
        } catch (MessagingException e) {
            throw new CustomException("Failed to send verification email: " + e.getMessage());
        }
    }

    /**
     * Generate a unique username based on the user's full name.
     *
     * @param fullName the user's full name
     * @return a unique username
     */
    private String generateUniqueUsername(String fullName) {
        // Step 1: Truncate or clean the full name
        String baseName = fullName.replaceAll("\\s+", "").length() > 10
                ? fullName.replaceAll("\\s+", "").substring(0, 10)
                : fullName.replaceAll("\\s+", ""); // Remove spaces and limit to 10 characters

        // Step 2: Generate a unique username with a random suffix
        String username;
        do {
            username = baseName + "_" + UUID.randomUUID().toString().substring(0, 6);
        } while (userRepo.existsByUsername(username)); // Check for uniqueness

        // Step 3: Return the unique username
        return username;
    }

    /**
     * Verify a user's email.
     *
     * @param email the email to verify
     * @throws CustomException if user not found
     */
    @Transactional
    public void verifyUser(String email) {
        User user = userRepo.findByemail(email);
        if (user == null) {
            throw new CustomException("User not found");
        }
        user.setVerificationStatus(User.VerificationStatus.Verified);
        userRepo.save(user);
    }

    /**
     * Get user by username.
     *
     * @param username the username to search for
     * @return the user with the given username, or null if not found
     */
    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    /**
     * Get user ID by username.
     *
     * @param username the username to search for
     * @return the user ID, or null if user not found
     */
    @Transactional(readOnly = true)
    public Long getUserIdByUsername(String username) {
        User user = userRepo.findByUsername(username);
        return user != null ? user.getId() : null;
    }
    /**
     * Get all users in the system.
     *
     * @return list of all users as DTOs
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepo.findAll();

        return users.stream().map(this::mapToDTO).collect(Collectors.toList());
    }


    /**
     * Get user by ID.
     *
     * @param id the user ID
     * @return the user as DTO
     * @throws CustomException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new CustomException("User not found"));
        return mapToDTO(user);
    }

    /**
     * Map DTO to entity.
     *
     * @param dto the user registration DTO
     * @return the user entity
     */
    private User mapToEntity(UserRegistrationDTO dto) {
        return new User(
                dto.getFullName(), dto.getPhoneNumber(), dto.getProfilePicture(), dto.getAddress(),
                dto.getCity(), dto.getState(), dto.getCountry(), dto.getPincode(),
                dto.getPreferences()
        );
    }

    /**
     * Map entity to DTO.
     *
     * @param user the user entity
     * @return the user response DTO
     */
    public UserResponseDTO mapToDTO(User user) {
        List<Long> favoritePropertyIds = propertyFavoriteRepository.findByUserId(user.getId())
                .stream()
                .map(fav -> fav.getProperty().getId())
                .collect(Collectors.toList());

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePicture(user.getProfilePicture())
                .address(user.getAddress())
                .city(user.getCity())
                .state(user.getState())
                .country(user.getCountry())
                .pincode(user.getPincode())
                .parent_code(user.getParentCode())
                .referCode(user.getReferCode())
                .preferences(user.getPreferences())
                .verificationStatus(user.getVerificationStatus())
                .verificationMethod(user.getVerificationMethod())
                .status(user.getStatus())
                .lastLogin(user.getLastLogin())
                .registrationDate(user.getRegistrationDate())
                .createdAt(user.getCreatedAt())
                .updateAt(user.getUpdatedAt())
                .favoritePropertyIds(favoritePropertyIds)
                .userRole(user.getUserRole().getName())
                .build();
    }


    /**
     * Update user information.
     *
     * @param id the user ID
     * @param userDTO the updated user information
     * @return the updated user entity
     * @throws CustomException if user not found
     */
    @Transactional
    public User updateUser(Long id, UserRegistrationDTO userDTO) {
        User existingUser = userRepo.findById(id)
                .orElseThrow(() -> new CustomException("User not found"));

        // Updating fields from DTO to User entity
        existingUser.setFullName(userDTO.getFullName());
        existingUser.setEmail(userDTO.getEmail());

        // Check if password is provided, otherwise keep the old password
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        existingUser.setProfilePicture(userDTO.getProfilePicture());
        existingUser.setAddress(userDTO.getAddress());
        existingUser.setCity(userDTO.getCity());
        existingUser.setState(userDTO.getState());
        existingUser.setCountry(userDTO.getCountry());
        existingUser.setPincode(userDTO.getPincode());
        existingUser.setPreferences(userDTO.getPreferences());

        return userRepo.save(existingUser);
    }


    /**
     * Update user's last login time.
     *
     * @param user the user to update
     */
    @Transactional
    public void updateUserLastLogin(User user) {
        user.setLastLogin(Timestamp.valueOf(LocalDateTime.now()));
        userRepo.save(user);
    }

    /**
     * Check if user has admin role.
     *
     * @param username the username to check
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin(String username) {
        User user = userRepo.findByUsername(username);

        if (user == null) {
            return false;
        }
        // Directly compare role name
        return "ROLE_ADMIN".equals(user.getUserRole().getName());
    }

    /**
     * Find the agent with the least number of assigned leads.
     *
     * @return the agent with least leads, or null if no agents
     */
    public User findLeastAssignedAgent() {
        Long id = 1L;
        List<User> agents = userRepo.findByUserRoleId(id); // Fetch all agents
        if (agents.isEmpty()) return null;

        return agents.stream()
                .min(Comparator.comparingInt(agent -> leadRepository.countByAgent(agent)))
                .orElse(null);
    }
    /**
     * Update user's role.
     *
     * @param newRoleId the new role ID
     * @param username the username of the user
     * @throws CustomException if user or role not found
     */
    @Transactional
    public void updateRoleId(Long newRoleId, String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new CustomException("User not found with username: " + username);
        }
        Long oldRoleId = user.getUserRole().getId();
        UserRole newRole = roleRepository.findById(newRoleId)
                .orElseThrow(() -> new CustomException("Role not found with roleId: " + newRoleId));

        user.setUserRole(newRole);
        userRepo.save(user);
    }

    /**
     * Update the status of a user to inactive or active.
     * This method is typically used during logout or login to mark the user status
     *
     * @param user the user whose status needs to be updated
     * @throws CustomException if the user is null
     */
    @Transactional
    public void updateUserStatus(User user) {
        // Validate user is not null
        if (user == null) {
            throw new CustomException("User not found");
        }
        // Save the updated user to the database
        userRepo.save(user);
    }

    /**
     * Get username by phone number.
     *
     * @param phoneNumber the phone number to search for
     * @return the username, or null if user not found
     */
    public String getUsernameByPhoneNumber(String phoneNumber) {
        User user = userRepo.findByPhoneNumber(phoneNumber);
        return (user != null) ? user.getUsername() : null;
    }

    /**
     * Get user by phone number.
     *
     * @param phoneNumber the phone number to search for
     * @return the user, or null if not found
     */
    public User getUserByPhoneNumber(String phoneNumber) {
        return userRepo.findByPhoneNumber(phoneNumber);
    }

    /**
     * Find user by ID.
     *
     * @param id the user ID
     * @return the user
     * @throws CustomException if user not found
     */
    public User findById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new CustomException("User Not found"));
    }

    /**
     * Generic method to verify OTP for both login and registration.
     *
     * @param phoneNumber the phone number to verify OTP against
     * @param otpEntered the OTP entered by the user
     * @param otpStorage the storage map where OTPs are stored
     * @return boolean indicating if OTP is valid
     */
    private boolean verifyOtp(String phoneNumber, String otpEntered, Map<String, String> otpStorage) {
        // Check if phone number exists in the provided OTP storage
        if (!otpStorage.containsKey(phoneNumber)) {
            return false;
        }

        // Get stored OTP for the phone number
        String storedOtp = otpStorage.get(phoneNumber);

        // Compare entered OTP with stored OTP
        if (storedOtp != null && storedOtp.equals(otpEntered)) {
            // Remove OTP from storage after successful verification
            otpStorage.remove(phoneNumber);
            return true;
        }
        return false;
    }

    /**
     * Verify OTP for user login.
     */
    public boolean verifyLoginOtp(String phoneNumber, String otpEntered) {
        return verifyOtp(phoneNumber, otpEntered, otpStorage);
    }
    /**
     * Verify OTP for forget password.
     */
    public boolean verifyOtp(String phoneNumber, String otpEntered) {
        return verifyOtp(phoneNumber, otpEntered, otpStorage);
    }
    /**
     * Verify OTP for user registration.
     */
    public boolean verifyRegistrationOtp(String phoneNumber, String otpEntered) {
        boolean isValid = verifyOtp(phoneNumber, otpEntered, registrationOtpStorage);

        if (isValid) {
            // Update user verification status
            User user = userRepo.findByPhoneNumber(phoneNumber);
            if (user != null) {
                user.setVerificationStatus(User.VerificationStatus.Verified);
                userRepo.save(user);
            }
        }
        return isValid;
    }

    public String getUsernameByemail(String email) {
        User user = userRepo.findByemail(email);
        return (user != null) ? user.getUsername() : null;
    }


    /**
     * Get user by email
     *
     * @param email The email to search for
     * @return The User if found, null otherwise
     */
    public User getUserByEmail(String email) {
        // Implementation depends on your repository
        return userRepo.findByemail(email);
    }
    /**
     * Save a password reset token for a user
     *
     * @param userId The ID of the user
     * @param resetToken The token to save
     */
    @Transactional
    public void saveToken(Long userId, String resetToken) {
        // Create expiration time (e.g., 1 hour from now)
        Instant expiryDate = Instant.now().plus(1, ChronoUnit.HOURS);

        // Create and save the token entity
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(resetToken);
        token.setUserId(userId);
        token.setExpiryDate(expiryDate);

        passwordResetTokenRepository.save(token);
    }

    /**
     * Verify a password reset token
     *
     * @param token The token to verify
     * @return The user ID if the token is valid, null otherwise
     */
    public Long verifyPasswordResetToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);

        if (resetToken == null) {
            return null; // Token does not exist
        }
        if (resetToken.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalStateException("Token has expired. Please request a new one.");
        }
        return resetToken.getUserId();
    }

  @Transactional
    public void updatePassword(Long userId, String newPassword) {
        if (!isValidPassword(newPassword)) {
            System.out.println();
            throw new CustomException("Password does not meet security requirements");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    // Example Password Validation Function
    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*[A-Z].*") && password.matches(".*\\d.*");
    }
    @Transactional
    public User findByPhoneNumber(String phoneNumber) {
         return userRepo.findByPhoneNumber(phoneNumber);
    }

    public ResponseEntity<?> notifyUser(User user, Phase phase) {
        return switch (phase) {
            case REGISTRATION -> sendVerificationLinkAndResponse(user);
            case LOGIN -> sendLoginResponse(user);
            default -> ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid notification phase"));
        };
    }

    private ResponseEntity<?> sendVerificationLinkAndResponse(User user) {
        String verificationType;
        String verificationUrl;

        if (user.getVerificationMethod() == User.VerificationMethod.Phone) {
            // Phone verification scenario
            String otp = generateAndStoreOtp(user.getPhoneNumber());
            // Generate and send OTP for phone verification
            // Here you would integrate with SMS service to send the OTP
            // smsService.sendOtp(userDTO.getPhoneNumber(), otp);
            // Resend verification OTP via SMS
            //        SMS_Service.sendSMS(user.getPhoneNumber());
            verificationType = "SMS";
            verificationUrl = "http://karynest-real-state.azurewebsites.net/v1/verify-user-otp";
        } else {
            // Email verification scenario
            String token = GenerateToken(user);
            verificationType = "email";
            verificationUrl = "http://karynest-real-state.azurewebsites.net/v1/verify?email=" +
                    user.getEmail() + "&token=" + token;
        }

        return ResponseEntity.ok()
                .body(Map.of(
                        "verificationType", verificationType,
                        "verificationUrl", verificationUrl,
                        "message", "Welcome to KarynEst Real Estate!",
                        "details", "Your account has been successfully registered, but requires verification to unlock full platform features.",
                        "verificationStatus", "Pending",
                        "userId", user.getId(),
                        "username", user.getUsername(),
                        "actionRequired", "Please verify your " + verificationType + " to enjoy all our application services.",
                        "benefits", List.of(
                                "Access to full property listings",
                                "Personalized recommendations",
                                "Communication with agents",
                                "Saving favorite properties",
                                "Exclusive market insights"
                        ),
                        "helpMessage", "Need assistance? Contact our support team at support@karynest.com"
                ));
    }
    private ResponseEntity<?> sendLoginResponse(User user) {
        String verificationType;
        String verificationUrl;

        if (user.getVerificationMethod() == User.VerificationMethod.Phone) {
            // Phone verification scenario
            String otp = generateAndStoreOtp(user.getPhoneNumber());
            // Here you would integrate with SMS service to send the OTP
            verificationType = "SMS";
            verificationUrl = "http://karynest-real-state.azurewebsites.net/v1/verify-user-otp";
        } else {
            // Email verification scenario
            String token = GenerateToken(user);
            verificationType = "email";
            verificationUrl = "http://karynest-real-state.azurewebsites.net/v1/verify?email=" +
                    user.getEmail() + "&token=" + token;
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "verificationType", verificationType,
                        "verificationUrl", verificationUrl,
                        "verificationStatus", "Your account is not verified.",
                        "message", "Please complete the verification process.",
                        "userId", user.getId(),
                        "username", user.getUsername(),
                        "verificationMethod", user.getVerificationMethod().name(),
                        "actionRequired", "Please verify your " + verificationType + " to unlock full platform features."
                ));
    }
    public String GenerateToken(User user)
    {
        // Generate reset token
        String Token = UUID.randomUUID().toString();
        // Store token in database with expiration
        saveToken(user.getId(), Token);
        // Send verification email
        sendVerificationEmail(user.getEmail(),Token);
        return Token;
    }

    /**
     * Updates the user preferences for a given user ID.
     *
     * @param userId The ID of the user whose preferences are being updated.
     * @param userPreferencesDTO The DTO containing the new preferences.
     * @return The updated user response DTO.
     */
    @Transactional
    public UserResponseDTO updateUserPreferences(Long userId, UserPreferencesDTO userPreferencesDTO) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new CustomException("User not found"));
        try {
            String jsonPreferences = new ObjectMapper().writeValueAsString(userPreferencesDTO.getPreferences());
            user.setPreferences(jsonPreferences);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error while converting preferences to JSON", e);
        }
        User updatedUser = userRepo.save(user);
        return mapToDTO(updatedUser);
    }
    public List<User> getParentUser(Long parentId) {
      return  userRepo.findByParentCode(parentId);
    }
    public String referCodeGenerater() {
        StringBuilder referCode = new StringBuilder(6);
        java.util.Random random = new java.util.Random();

        // Preset the character set
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int charLength = characters.length();

        // Pre-allocate capacity = 6
        for (int i = 0; i < 6; i++) {
            referCode.append(characters.charAt(random.nextInt(charLength)));
        }

        return referCode.toString();
    }
}

package com.backend.karyanestApplication.Controller;

import com.backend.karyanestApplication.DTO.UserActivityLogResponseDTO;
import com.example.Authentication.UTIL.JwtUtil;
import com.backend.karyanestApplication.Service.UserActivityLogService;
import com.backend.karyanestApplication.Service.UserService;
import com.example.Authentication.Component.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/user_activity")
public class UserActivityLogController {

    @Autowired
    private UserActivityLogService activityLogService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserContext userContext;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @GetMapping("/logs")
    public ResponseEntity<?> getCurrentUserActivityLogs(HttpServletRequest request) {
        try {
            String token = userContext.extractToken(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication token not found"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Long userId = userService.getUserIdByUsername(username);
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
            }
            
            List<UserActivityLogResponseDTO> logs = activityLogService.getUserActivityLogs(userId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve activity logs", "details", e.getMessage()));
        }
    }
    @GetMapping  // Changed to GET since we're using request parameters now
    public ResponseEntity<?> getUserActivityLogsByType(
            HttpServletRequest request,
            @RequestParam("activityType") String activityType) {  // Changed to @RequestParam
        try {
            // Validation for activity type
            if (activityType == null || activityType.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Activity type is required"));
            }

            String token = userContext.extractToken(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication token not found"));
            }

            String username = jwtUtil.extractUsername(token);
            Long userId = userService.getUserIdByUsername(username);

            if (userId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            List<UserActivityLogResponseDTO> logs = activityLogService.getUserActivityLogsByType(userId, activityType);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve activity logs", "details", e.getMessage()));
        }
    }
    @GetMapping("/logs/recent")
    public ResponseEntity<?> getRecentUserActivityLogs(
            HttpServletRequest request,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            String token = userContext.extractToken(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication token not found"));
            }
            
            String username = jwtUtil.extractUsername(token);
            Long userId = userService.getUserIdByUsername(username);
            
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
            }
            
            List<UserActivityLogResponseDTO> logs = activityLogService.getRecentUserActivityLogs(userId, limit);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve activity logs", "details", e.getMessage()));
        }
    }
    
    // Admin endpoint to view any user's logs
    @GetMapping("{id}")
    public ResponseEntity<?> getUserActivityLogsByAdmin(@PathVariable Long id) {
        try {
            List<UserActivityLogResponseDTO> logs = activityLogService.getUserActivityLogs(id);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to retrieve activity logs", "details", e.getMessage()));
        }
    }
}
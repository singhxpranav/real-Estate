package com.example.Authentication.Middleware;

import com.example.Authentication.DTO.JWTUserDTO;
import com.example.Authentication.Service.Auth;
//import com.example.Authentication.Service.JwtService;
import com.example.Authentication.UTIL.JwtUtil;
import com.example.Authentication.UTIL.MyEncryptionUtil;
import com.example.rbac.Service.PermissionsService;
import com.example.Authentication.Component.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

//
//@Component
//public class AuthorizationFilter extends OncePerRequestFilter {
//
//    @Autowired
//    private UserContext userContext; // Using UserContext instead of JwtUtil
//
//    @Autowired
//    private PermissionsService permissionService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
//            throws ServletException, IOException {
//
//        String path = request.getServletPath();
//        System.out.println("Request Path: " + path);
//
//        // Bypass authentication for certain endpoints
//        if (path.matches("^/v1/auth/.*") || path.matches("^/v1/home/.*") ||
//                path.matches("^/v3/api-docs.*") || path.matches("^/swagger-ui/.*") ||
//                path.matches("^/swagger-ui.html$") || path.matches("^/rbac/test/.*")) {
//
//            chain.doFilter(request, response);
//            return;
//        }
//
//        // Extract and validate user
//        String username = userContext.validateAndExtractUser(request, response);
//        if (username == null) {
//            return; // Response already sent in case of failure
//        }
//
//        // Extract role separately
//        String role = userContext.extractRole(request);
//        String fullName=userContext.extractFullname(request);
//        if (role == null) {
//            userContext.sendUnauthorizedResponse(response, "Authorization failed: Role not found in token");
//            return;
//        }
//
//        if (!permissionService.checkPermission(role, path, username)) {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            response.setContentType("application/json");
//            response.getWriter().write("{\"error\": \"Access Denied: You do not have permission to view this resource or access data belonging to another.\"}");
//            return;
//        }
//
//        // Authenticate the user
//        GrantedAuthority authority = userContext.getUserRoleAuthority(request);
//        UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(username, null, Collections.singletonList(authority));
//        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//        SecurityContextHolder.getContext().setAuthentication(authToken);
//       Long userId = userContext.extractJWTValue(request, "id", Long.class);
//        JWTUserDTO user = new JWTUserDTO(userId, username, role,fullName);
//        request.setAttribute("user", user);
//        chain.doFilter(request, response);
//    }
//}
@Component
public class AuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private UserContext userContext;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PermissionsService permissionService; // optional if not used anymore

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Allowlisted paths
        if (path.matches("^/v1/auth/.*") || path.matches("^/v1/home/.*") ||
                path.matches("^/v3/api-docs.*") || path.matches("^/swagger-ui/.*") ||
                path.matches("^/swagger-ui.html$") || path.matches("^/internal.*")) {

            filterChain.doFilter(request, response);
            return;
        }

        // JWT se validate & extract user
        String username = userContext.validateAndExtractUser(request, response);
        if (username == null) return;

        String role = userContext.extractRole(request);
        String fullName = userContext.extractFullname(request);
        if (role == null) {
            userContext.sendUnauthorizedResponse(response, "Authorization failed: Role not found in token");
            return;
        }
        String token = userContext.extractToken(request);
        List<String> encryptedPermissions = jwtUtil.extractPermissions(token);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            List<GrantedAuthority> authorities = encryptedPermissions.stream()
                    .map(MyEncryptionUtil::decrypt)  // 👈 Decryption
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }


        // Setting user object in request for downstream use
        Long userId = userContext.extractJWTValue(request, "id", Long.class);
        JWTUserDTO user = new JWTUserDTO(userId, username, role, fullName);
        request.setAttribute("user", user);

        filterChain.doFilter(request, response);
    }
}

package com.backend.karyanestApplication.Configration;

import com.backend.karyanestApplication.Middleware.AuthenticationFilter;
import com.backend.karyanestApplication.Middleware.AuthorizationFilter;
import com.backend.karyanestApplication.Middleware.LoginLoggingFilter;
import com.backend.karyanestApplication.Middleware.RequestLoggingFilter;
import com.backend.karyanestApplication.Service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


@Configuration
@EnableWebSecurity
@EnableWebMvc
public class SecurityConfig {

    private final AuthenticationFilter authenticationFilter;
    private final AuthorizationFilter authorizationFilter;
    private final LoginLoggingFilter loginLoggingFilter;
    private final RequestLoggingFilter requestLoggingFilter;

    @Autowired
    private MyUserDetailsService userDetailsService;

    public SecurityConfig(AuthenticationFilter authenticationFilter,
                          AuthorizationFilter authorizationFilter,
                          LoginLoggingFilter loginLoggingFilter,
                          RequestLoggingFilter requestLoggingFilter) {
        this.authenticationFilter = authenticationFilter;
        this.authorizationFilter = authorizationFilter;
        this.loginLoggingFilter = loginLoggingFilter;
        this.requestLoggingFilter = requestLoggingFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publicly accessible endpoints
                        .requestMatchers("/v1/auth/**", "/v1/home/**","/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

//                        // Allow Swagger in dev, restrict in prod
//                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").hasRole("ADMIN") // Optional

                        // Role-based access control
                        .requestMatchers("/v1/admin/**").hasRole("ADMIN")  // Only admins
                        .requestMatchers("/v1/agent/**").hasRole("AGENT")  // Only agents
                        .requestMatchers("/v1/user/**").hasAnyRole("USER", "AGENT", "ADMIN") // General users

                        // Catch-all for authenticated users
                        .anyRequest().authenticated()
                )
                // Correct filter order
                .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(authorizationFilter, AuthenticationFilter.class)
                .addFilterAfter(loginLoggingFilter, AuthorizationFilter.class)
                .addFilterAfter(requestLoggingFilter, LoginLoggingFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return daoAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

//package com.backend.karyanestApplication.Model;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.Instant;
//
///**
// * Password Reset Token entity class
// */
//@Entity
//@Data
//@AllArgsConstructor
////@NoArgsConstructor
//public class PasswordResetToken {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String token;
//
//    private Long userId;
//
//    private Instant expiryDate;
//}
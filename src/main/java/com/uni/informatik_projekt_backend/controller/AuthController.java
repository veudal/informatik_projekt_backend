package com.uni.informatik_projekt_backend.controller;

import com.uni.informatik_projekt_backend.model.User;
import com.uni.informatik_projekt_backend.repository.UserRepository;
import com.uni.informatik_projekt_backend.security.CustomUserDetailsService;
import com.uni.informatik_projekt_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private CustomUserDetailsService userDetailsService;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        if (!user.getRole().equals("parent") && !user.getRole().equals("child")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Role must be parent or child"));
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setCreatedAt(new Date());
        User saved = userRepository.save(user);
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return ResponseEntity.ok(Map.of("token", jwtUtil.generateToken(details), "user", saved));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User login) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPasswordHash()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
        UserDetails details = userDetailsService.loadUserByUsername(login.getEmail());
        User user = userRepository.findByEmail(login.getEmail()).orElseThrow();
        return ResponseEntity.ok(Map.of("token", jwtUtil.generateToken(details), "user", user));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
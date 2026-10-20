package com.uni.informatik_projekt_backend.controller;

import com.uni.informatik_projekt_backend.model.LoginUser;
import com.uni.informatik_projekt_backend.model.User;
import com.uni.informatik_projekt_backend.repository.UserRepository;
import com.uni.informatik_projekt_backend.security.CustomUserDetailsService;
import com.uni.informatik_projekt_backend.security.JwtUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

    /**
     * Client sends plaintext password over HTTPS: {"email","password","role","displayName","familyId"}
     * @JsonProperty("password") on User.passwordHash maps JSON "password" -> Java field passwordHash
     * The server BCrypt-hashes it before storing. Never stores or returns plaintext.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        if (!user.getRole().equals("parent") && !user.getRole().equals("child")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Role must be parent or child"));
        }
        // passwordHash temporarily holds the plaintext password from the JSON request
        String plainPassword = user.getPasswordHash();
        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setCreatedAt(new Date());
        User saved = userRepository.save(user);
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return ResponseEntity.ok(Map.of("token", jwtUtil.generateToken(details), "user", saved));
    }

    /**
     * Client sends plaintext password: {"email","password"}
     * Spring Security compares plaintext against stored BCrypt hash.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUser login) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
        UserDetails details = userDetailsService.loadUserByUsername(login.getEmail());
        User user = userRepository.findByEmail(login.getEmail()).orElseThrow();
        return ResponseEntity.ok(Map.of("token", jwtUtil.generateToken(details), "user", user));
    }

    /**
     * Requires valid JWT. Lists all users (passwords never exposed).
     */
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
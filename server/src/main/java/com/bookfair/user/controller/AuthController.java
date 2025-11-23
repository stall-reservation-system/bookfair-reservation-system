package com.bookfair.user.controller;

import com.bookfair.user.model.User;
import com.bookfair.user.repository.UserRepository;
import com.bookfair.user.security.JwtUtil;
import com.bookfair.user.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ================= Register Endpoint =================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getBusinessId(),
                    request.getInviteCode(),
                    request.getContactNumber(),
                    request.getRole()
            );

            // Return user and success message
            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful!",
                    "user", user
            ));

        } catch (RuntimeException e) {
            // Return 400 with backend error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ================= Login Endpoint =================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "token", token,
                "user", user
        ));
    }

    // ================= Exception Handling =================
    // Optional: global fallback for unhandled runtime exceptions
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
    }

    // ================= Request DTOs =================
    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;
        private Integer businessId;
        private String inviteCode;
        private String contactNumber;
        private String role;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public Integer getBusinessId() { return businessId; }
        public void setBusinessId(Integer businessId) { this.businessId = businessId; }

        public String getInviteCode() { return inviteCode; }
        public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }

        public String getContactNumber() { return contactNumber; }
        public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}

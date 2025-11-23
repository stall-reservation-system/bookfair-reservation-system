package com.bookfair.user.service;

import com.bookfair.user.model.Business;
import com.bookfair.user.model.User;
import com.bookfair.user.repository.BusinessRepository;
import com.bookfair.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BusinessRepository businessRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User register(
            String name,
            String email,
            String password,
            Integer businessId,
            String inviteCode,
            String contactNumber,
            String role
    ) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found."));

        // if (!passwordEncoder.matches(inviteCode, business.getInviteCodeHash())) {
        //     throw new RuntimeException("Invalid invite code for selected business.");
        // }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setBusiness(business);
        user.setContactNumber(contactNumber);
        user.setRole(role);

        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));
     

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password.");
        }

        return user;
    }
}

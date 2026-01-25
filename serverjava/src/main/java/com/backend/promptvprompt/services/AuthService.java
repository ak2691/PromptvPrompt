package com.backend.promptvprompt.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.promptvprompt.DTO.Auth.AuthResponse;
import com.backend.promptvprompt.DTO.Auth.LoginRequest;
import com.backend.promptvprompt.DTO.Auth.RegistrationRequest;
import com.backend.promptvprompt.exceptions.InvalidCredentialsException;
import com.backend.promptvprompt.exceptions.UserAlreadyExistsException;
import com.backend.promptvprompt.models.User;
import com.backend.promptvprompt.models.UserProfile;
import com.backend.promptvprompt.repos.UserProfileRepo;
import com.backend.promptvprompt.repos.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepo userRepo;
    private final UserProfileRepo userProfileRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegistrationRequest request) {
        // Check if email already exists
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        // Check if username already exists
        if (userProfileRepo.existsByDisplayName(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken");
        }

        // Create and save User entity
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isEmailVerified(false)
                .build();

        User savedUser = userRepo.save(user);

        // Create and save UserProfile entity
        UserProfile userProfile = UserProfile.builder()
                .user(savedUser)
                .displayName(request.getUsername())
                .gamesPlayed(0)
                .wins(0)
                .losses(0)
                .draws(0)
                .dailyGamesPlayed(0)
                .build();

        userProfileRepo.save(userProfile);

        // Return response
        return AuthResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .username(userProfile.getDisplayName())
                .message("User registered successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Find user profile by username
        UserProfile userProfile = userProfileRepo.findByDisplayName(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));

        // Get the associated user
        User user = userProfile.getUser();

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        // Return response
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(userProfile.getDisplayName())
                .message("Login successful")
                .build();
    }
}

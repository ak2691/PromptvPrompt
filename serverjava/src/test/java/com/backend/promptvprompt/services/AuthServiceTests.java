package com.backend.promptvprompt.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.backend.promptvprompt.DTO.Auth.AuthResponse;
import com.backend.promptvprompt.DTO.Auth.LoginRequest;
import com.backend.promptvprompt.DTO.Auth.RegistrationRequest;
import com.backend.promptvprompt.exceptions.InvalidCredentialsException;
import com.backend.promptvprompt.exceptions.UserAlreadyExistsException;
import com.backend.promptvprompt.models.User;
import com.backend.promptvprompt.models.UserProfile;
import com.backend.promptvprompt.repos.UserProfileRepo;
import com.backend.promptvprompt.repos.UserRepo;
import com.backend.promptvprompt.services.AuthService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

        @Mock
        private UserRepo userRepo;

        @Mock
        private UserProfileRepo userProfileRepo;

        @Mock
        private BCryptPasswordEncoder passwordEncoder;

        @InjectMocks
        private AuthService authService;

        private RegistrationRequest registrationRequest;
        private LoginRequest loginRequest;
        private User user;
        private UserProfile userProfile;

        @BeforeEach
        void setUp() {
                // Setup test data
                registrationRequest = new RegistrationRequest(
                                "test@example.com",
                                "testuser",
                                "password123");

                loginRequest = new LoginRequest(
                                "testuser",
                                "password123");

                user = User.builder()
                                .id("user-123")
                                .email("test@example.com")
                                .passwordHash("hashedPassword")
                                .isEmailVerified(false)
                                .build();

                userProfile = UserProfile.builder()
                                .id("profile-123")
                                .user(user)
                                .displayName("testuser")
                                .gamesPlayed(0)
                                .wins(0)
                                .losses(0)
                                .draws(0)
                                .dailyGamesPlayed(0)
                                .build();
        }

        // ========== REGISTRATION TESTS ==========

        @Test
        @DisplayName("Should successfully register a new user with valid credentials")
        void register_Success() {
                // Arrange
                when(userRepo.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
                when(userProfileRepo.existsByDisplayName(registrationRequest.getUsername())).thenReturn(false);
                when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("hashedPassword");
                when(userRepo.save(any(User.class))).thenReturn(user);
                when(userProfileRepo.save(any(UserProfile.class))).thenReturn(userProfile);

                // Act
                AuthResponse response = authService.register(registrationRequest);

                // Assert
                assertNotNull(response);
                assertEquals(user.getId(), response.getUserId());
                assertEquals(user.getEmail(), response.getEmail());
                assertEquals(userProfile.getDisplayName(), response.getUsername());
                assertEquals("User registered successfully", response.getMessage());

                // Verify interactions
                verify(userRepo).existsByEmail(registrationRequest.getEmail());
                verify(userProfileRepo).existsByDisplayName(registrationRequest.getUsername());
                verify(passwordEncoder).encode(registrationRequest.getPassword());
                verify(userRepo).save(any(User.class));
                verify(userProfileRepo).save(any(UserProfile.class));
        }

        @Test
        @DisplayName("Should throw UserAlreadyExistsException when email is already registered")
        void register_EmailAlreadyExists_ThrowsException() {
                // Arrange
                when(userRepo.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

                // Act & Assert
                UserAlreadyExistsException exception = assertThrows(
                                UserAlreadyExistsException.class,
                                () -> authService.register(registrationRequest));

                assertEquals("Email is already registered", exception.getMessage());

                // Verify that we only checked email existence and didn't proceed further
                verify(userRepo).existsByEmail(registrationRequest.getEmail());
                verify(userProfileRepo, never()).existsByDisplayName(any());
                verify(passwordEncoder, never()).encode(any());
                verify(userRepo, never()).save(any());
                verify(userProfileRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should throw UserAlreadyExistsException when username is already taken")
        void register_UsernameAlreadyExists_ThrowsException() {
                // Arrange
                when(userRepo.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
                when(userProfileRepo.existsByDisplayName(registrationRequest.getUsername())).thenReturn(true);

                // Act & Assert
                UserAlreadyExistsException exception = assertThrows(
                                UserAlreadyExistsException.class,
                                () -> authService.register(registrationRequest));

                assertEquals("Username is already taken", exception.getMessage());

                // Verify interactions
                verify(userRepo).existsByEmail(registrationRequest.getEmail());
                verify(userProfileRepo).existsByDisplayName(registrationRequest.getUsername());
                verify(passwordEncoder, never()).encode(any());
                verify(userRepo, never()).save(any());
                verify(userProfileRepo, never()).save(any());
        }

        @Test
        @DisplayName("Should create User entity with correct properties during registration")
        void register_VerifyUserEntityCreatedCorrectly() {
                // Arrange
                when(userRepo.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
                when(userProfileRepo.existsByDisplayName(registrationRequest.getUsername())).thenReturn(false);
                when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("hashedPassword");
                when(userRepo.save(any(User.class))).thenReturn(user);
                when(userProfileRepo.save(any(UserProfile.class))).thenReturn(userProfile);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

                // Act
                authService.register(registrationRequest);

                // Assert
                verify(userRepo).save(userCaptor.capture());
                User capturedUser = userCaptor.getValue();

                assertEquals(registrationRequest.getEmail(), capturedUser.getEmail());
                assertEquals("hashedPassword", capturedUser.getPasswordHash());
                assertFalse(capturedUser.getIsEmailVerified());
        }

        @Test
        @DisplayName("Should create UserProfile entity with correct initial values during registration")
        void register_VerifyUserProfileCreatedCorrectly() {
                // Arrange
                when(userRepo.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
                when(userProfileRepo.existsByDisplayName(registrationRequest.getUsername())).thenReturn(false);
                when(passwordEncoder.encode(registrationRequest.getPassword())).thenReturn("hashedPassword");
                when(userRepo.save(any(User.class))).thenReturn(user);
                when(userProfileRepo.save(any(UserProfile.class))).thenReturn(userProfile);

                ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);

                // Act
                authService.register(registrationRequest);

                // Assert
                verify(userProfileRepo).save(profileCaptor.capture());
                UserProfile capturedProfile = profileCaptor.getValue();

                assertEquals(user, capturedProfile.getUser());
                assertEquals(registrationRequest.getUsername(), capturedProfile.getDisplayName());
                assertEquals(0, capturedProfile.getGamesPlayed());
                assertEquals(0, capturedProfile.getWins());
                assertEquals(0, capturedProfile.getLosses());
                assertEquals(0, capturedProfile.getDraws());
                assertEquals(0, capturedProfile.getDailyGamesPlayed());
        }

        // ========== LOGIN TESTS ==========

        @Test
        @DisplayName("Should successfully login user with valid username and password")
        void login_Success() {
                // Arrange
                when(userProfileRepo.findByDisplayName(loginRequest.getUsername()))
                                .thenReturn(Optional.of(userProfile));
                when(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash()))
                                .thenReturn(true);

                // Act
                AuthResponse response = authService.login(loginRequest);

                // Assert
                assertNotNull(response);
                assertEquals(user.getId(), response.getUserId());
                assertEquals(user.getEmail(), response.getEmail());
                assertEquals(userProfile.getDisplayName(), response.getUsername());
                assertEquals("Login successful", response.getMessage());

                // Verify interactions
                verify(userProfileRepo).findByDisplayName(loginRequest.getUsername());
                verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPasswordHash());
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when user is not found")
        void login_UserNotFound_ThrowsException() {
                // Arrange
                when(userProfileRepo.findByDisplayName(loginRequest.getUsername()))
                                .thenReturn(Optional.empty());

                // Act & Assert
                InvalidCredentialsException exception = assertThrows(
                                InvalidCredentialsException.class,
                                () -> authService.login(loginRequest));

                assertEquals("Invalid username or password", exception.getMessage());

                // Verify interactions
                verify(userProfileRepo).findByDisplayName(loginRequest.getUsername());
                verify(passwordEncoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when password is incorrect")
        void login_IncorrectPassword_ThrowsException() {
                // Arrange
                when(userProfileRepo.findByDisplayName(loginRequest.getUsername()))
                                .thenReturn(Optional.of(userProfile));
                when(passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash()))
                                .thenReturn(false);

                // Act & Assert
                InvalidCredentialsException exception = assertThrows(
                                InvalidCredentialsException.class,
                                () -> authService.login(loginRequest));

                assertEquals("Invalid username or password", exception.getMessage());

                // Verify interactions
                verify(userProfileRepo).findByDisplayName(loginRequest.getUsername());
                verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPasswordHash());
        }
}

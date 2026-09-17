package com.abodebase.api.auth.service;

import com.abodebase.api.auth.dto.RegisterRequest;
import com.abodebase.api.auth.entity.Role;
import com.abodebase.api.auth.entity.User;
import com.abodebase.api.auth.repository.RoleRepository;
import com.abodebase.api.auth.repository.UserRepository;
import com.abodebase.api.auth.validation.UsernameValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsernameValidator usernameValidator;

    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
            userRepository,
            roleRepository,
            passwordEncoder,
            usernameValidator
        );
    }

    @Test
    void validRegistrationCreatesUser() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("User123");
        request.setEmail("ALLISON@example.com");
        request.setPassword("Password123!");

        Role userRole = new Role("USER");

        when(usernameValidator.validateAndNormalize("User123"))
            .thenReturn("user123");

        when(userRepository.findByUsernameIgnoreCase("user123"))
            .thenReturn(Optional.empty());

        when(userRepository.findByEmailIgnoreCase("allison@example.com"))
            .thenReturn(Optional.empty());

        when(roleRepository.findByName("USER"))
            .thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode("Password123!"))
            .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        User result = registrationService.register(request);

        assertEquals("user123", result.getUsername());
        assertEquals("allison@example.com", result.getEmail());
        assertEquals("hashed-password", result.getPasswordHash());
        assertTrue(result.isEnabled());
        assertTrue(result.getRoles().contains(userRole));
    }

    @Test
    void duplicateUsernameIsRejected() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("User123");
        request.setEmail("allison@example.com");
        request.setPassword("Password123!");

        when(usernameValidator.validateAndNormalize("User123"))
            .thenReturn("user123");

        when(userRepository.findByUsernameIgnoreCase("user123"))
            .thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registrationService.register(request)
        );

        assertEquals(
            "Username is already taken.",
            exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void duplicateEmailIsRejected() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("User123");
        request.setEmail("allison@example.com");
        request.setPassword("Password123!");

        when(usernameValidator.validateAndNormalize("User123"))
            .thenReturn("user123");

        when(userRepository.findByUsernameIgnoreCase("user123"))
            .thenReturn(Optional.empty());

        when(userRepository.findByEmailIgnoreCase("allison@example.com"))
            .thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> registrationService.register(request)
        );

        assertEquals(
            "Email is already registered.",
            exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void passwordIsHashedBeforeSaving() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("User123");
        request.setEmail("allison@example.com");
        request.setPassword("Password123!");

        Role userRole = new Role("USER");

        when(usernameValidator.validateAndNormalize("User123"))
            .thenReturn("user123");

        when(userRepository.findByUsernameIgnoreCase("user123"))
            .thenReturn(Optional.empty());

        when(userRepository.findByEmailIgnoreCase("allison@example.com"))
            .thenReturn(Optional.empty());

        when(roleRepository.findByName("USER"))
            .thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode("Password123!"))
            .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        registrationService.register(request);

        ArgumentCaptor<User> userCaptor =
            ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("hashed-password", savedUser.getPasswordHash());
        assertNotEquals(
            "Password123!",
            savedUser.getPasswordHash()
        );
    }

    @Test
    void newUserReceivesUserRole() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("User123");
        request.setEmail("allison@example.com");
        request.setPassword("Password123!");

        Role userRole = new Role("USER");

        when(usernameValidator.validateAndNormalize("User123"))
            .thenReturn("user123");

        when(userRepository.findByUsernameIgnoreCase("user123"))
            .thenReturn(Optional.empty());

        when(userRepository.findByEmailIgnoreCase("allison@example.com"))
            .thenReturn(Optional.empty());

        when(roleRepository.findByName("USER"))
            .thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode("Password123!"))
            .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        User result = registrationService.register(request);

        assertTrue(result.getRoles().contains(userRole));
        assertFalse(
            result.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ADMIN"))
        );
    }

    @Test
    void missingUserRoleIsRejected() {

        RegisterRequest request = new RegisterRequest();
        request.setUsername("User123");
        request.setEmail("allison@example.com");
        request.setPassword("Password123!");

        when(usernameValidator.validateAndNormalize("User123"))
            .thenReturn("user123");

        when(userRepository.findByUsernameIgnoreCase("user123"))
            .thenReturn(Optional.empty());

        when(userRepository.findByEmailIgnoreCase("allison@example.com"))
            .thenReturn(Optional.empty());

        when(roleRepository.findByName("USER"))
            .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> registrationService.register(request)
        );

        assertEquals(
            "USER role was not found in the database.",
            exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }
}

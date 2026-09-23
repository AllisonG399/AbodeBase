package com.abodebase.api.auth.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.abodebase.api.auth.entity.User;
import com.abodebase.api.auth.repository.UserRepository;
import com.abodebase.api.auth.service.RegistrationRateLimitService;

import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Tests Include:
 * Authenticated User Can Request Account Deletion
 * Pending Deletion User Can Login and Access Current User
 * User Can Cancel Account Deletion
 * Account Deletion Requires Correct Password - Wrong Password
 * Authenticated User Can Permanently Delete Own Account - Hard Delete
 * Permanently Deleted User Cannot Login
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RegistrationRateLimitService registrationRateLimitService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void resetRegistrationRateLimiter() {
        registrationRateLimitService.resetAllAttempts();
    }
    
    // ============================================
    // Authenticated User Can Request Account Deletion
    // ============================================

    @Test
    void authenticatedUserCanRequestAccountDeletion() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());

        String username = "deleteuser" + timestamp;
        String email = "delete" + timestamp + "@example.com";
        String password = "Password123!";

        // Register
        String registerBody = """
            {
                "username": "%s",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(
            username,
            email,
            password
        );

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody)
        )
        .andExpect(status().isCreated());

        // Login
        String loginBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(
            email,
            password
        );

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody)
        )
        .andExpect(status().isOk());

        // Request account deletion
        mockMvc.perform(
            delete("/api/users/me")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "password": "%s"
                    }
                    """.formatted(password))
        )
        .andExpect(status().isNoContent());

        // Verify account is pending deletion
        User user = userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow();

        assertNotNull(user.getDeletionRequestedAt());
    }

    // ============================================
    // Pending Deletion User Can Login and Access Current User
    // ============================================

    @Test
    void pendingDeletionUserCanAccessCurrentUserEndpoint()
        throws Exception {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        String username =
            "deleteduser" + timestamp;

        String email =
            "deleted" + timestamp + "@example.com";

        String password =
            "Password123!";

        // Register
        String registerBody = """
            {
                "username": "%s",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(
                username,
                email,
                password
            );

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody)
        )
        .andExpect(status().isCreated());

        // Login
        String loginBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(
                email,
                password
            );

        MockHttpSession session =
            new MockHttpSession();

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody)
        )
        .andExpect(status().isOk());

        // Request account deletion
        mockMvc.perform(
            delete("/api/users/me")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "password": "%s"
                    }
                    """.formatted(password))
        )
        .andExpect(status().isNoContent());

        // The deletion request logs the user out.
        mockMvc.perform(
            get("/api/auth/me")
                .session(session)
        )
        .andExpect(status().isUnauthorized());

        // Login again after requesting deletion
        MockHttpSession newSession =
            new MockHttpSession();

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(newSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody)
        )
        .andExpect(status().isOk());

        // Verify the pending-deletion account can access /me
        mockMvc.perform(
            get("/api/auth/me")
                .session(newSession)
        )
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.deletionRequestedAt")
                .isNotEmpty()
        );
    }

    // ============================================
    // User Can Cancel Account Deletion
    // ============================================

    @Test
    void userCanCancelAccountDeletion() throws Exception {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        String username =
            "recoveruser" + timestamp;

        String email =
            "recover" + timestamp + "@example.com";

        String password =
            "Password123!";

        // Register
        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "%s",
                        "email": "%s",
                        "password": "%s"
                    }
                    """.formatted(
                        username,
                        email,
                        password
                    ))
        )
        .andExpect(status().isCreated());

        // Login
        MockHttpSession session =
            new MockHttpSession();

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "identifier": "%s",
                        "password": "%s"
                    }
                    """.formatted(
                        email,
                        password
                    ))
        )
        .andExpect(status().isOk());

        // Request account deletion
        mockMvc.perform(
            delete("/api/users/me")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "password": "%s"
                    }
                    """.formatted(password))
        )
        .andExpect(status().isNoContent());

        // Login again after requesting deletion
        MockHttpSession recoverySession =
            new MockHttpSession();

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(recoverySession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "identifier": "%s",
                        "password": "%s"
                    }
                    """.formatted(
                        email,
                        password
                    ))
        )
        .andExpect(status().isOk());

        // Verify account is pending deletion
        mockMvc.perform(
            get("/api/auth/me")
                .session(recoverySession)
        )
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.deletionRequestedAt")
                .isNotEmpty()
        );

        // Cancel account deletion
        mockMvc.perform(
            post("/api/users/me/cancel-deletion")
                .with(csrf())
                .session(recoverySession)
        )
        .andExpect(status().isNoContent());

        // Verify account is active again
        mockMvc.perform(
            get("/api/auth/me")
                .session(recoverySession)
        )
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.deletionRequestedAt")
                .doesNotExist()
        );
    }

    // ============================================
    // Account Deletion Requires Correct Password - Wrong Password
    // ============================================
    @Test
    void accountDeletionRequiresCorrectPassword() throws Exception {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        String username =
            "passworddelete" + timestamp;

        String email =
            "passworddelete" + timestamp + "@example.com";

        String password =
            "Password123!";

        // Register
        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "%s",
                        "email": "%s",
                        "password": "%s"
                    }
                    """.formatted(
                        username,
                        email,
                        password
                    ))
        )
        .andExpect(status().isCreated());

        // Login
        MockHttpSession session =
            new MockHttpSession();

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "identifier": "%s",
                        "password": "%s"
                    }
                    """.formatted(
                        email,
                        password
                    ))
        )
        .andExpect(status().isOk());

        // Attempt deletion with incorrect password
        mockMvc.perform(
            delete("/api/users/me")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "password": "WrongPassword123!"
                    }
                    """)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Authenticated User Can Permanently Delete Own Account
    // ============================================

    @Test
    void authenticatedUserCanPermanentlyDeleteOwnAccount() throws Exception {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "permanentdelete" + timestamp;
        String email = "permanentdelete" + timestamp + "@example.com";
        String password = "Password123!";

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "%s",
                        "email": "%s",
                        "password": "%s"
                    }
                    """.formatted(username, email, password))
        ).andExpect(status().isCreated());

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "identifier": "%s",
                        "password": "%s"
                    }
                    """.formatted(email, password))
        ).andExpect(status().isOk());

        mockMvc.perform(
            delete("/api/users/me/permanent")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "password": "%s"
                    }
                    """.formatted(password))
        ).andExpect(status().isNoContent());
    }

    // ============================================
    // Permanently Deleted User Cannot Login
    // ============================================

    @Test
    void permanentlyDeletedUserCannotLogin() throws Exception {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        String username =
            "goneuser" + timestamp;

        String email =
            "gone" + timestamp + "@example.com";

        String password =
            "Password123!";

        // Register
        String registerBody = """
            {
                "username": "%s",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(
                username,
                email,
                password
            );

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody)
        )
        .andExpect(status().isCreated());

        // Login
        String loginBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(
                email,
                password
            );

        MockHttpSession session =
            new MockHttpSession();

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody)
        )
        .andExpect(status().isOk());

        // Permanently delete account
        mockMvc.perform(
            delete("/api/users/me/permanent")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "password": "%s"
                    }
                    """.formatted(password))
        )
        .andExpect(status().isNoContent());

        // Attempt login after permanent deletion
        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody)
        )
        .andExpect(status().isUnauthorized());
    }

}

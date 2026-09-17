package com.abodebase.api.auth.controller;

import com.abodebase.api.auth.dto.LoginRequest;
import com.abodebase.api.auth.dto.RegisterRequest;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    // ============================================
    // Authenticated User Can Delete Account
    // ============================================

    @Test
    void authenticatedUserCanDeleteOwnAccount() throws Exception {

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

        // Delete account
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/api/users/me")
                .with(csrf())
                .session(session)
        )
        .andExpect(status().isNoContent());
    }

    // ============================================
    // Deleted User Cannot Access Current User
    // ============================================

    @Test
    void deletedUserCannotAccessCurrentUserEndpoint() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());

        String username = "deleteduser" + timestamp;
        String email = "deleted" + timestamp + "@example.com";
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

        // Delete Account
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/api/users/me")
                .with(csrf())
                .session(session)
        )
        .andExpect(status().isNoContent());

        // Verify Account is gone
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/auth/me")
                .session(session)
        )
        .andExpect(status().isUnauthorized());
    }

    // ============================================
    // Deleted User Cannot Login
    // ============================================

    @Test
    void deletedUserCannotLogin() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());

        String username = "goneuser" + timestamp;
        String email = "gone" + timestamp + "@example.com";
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

        // Delete Account
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .delete("/api/users/me")
                .with(csrf())
                .session(session)
        )
        .andExpect(status().isNoContent());

        // Attempt login after creation
        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody)
        )
        .andExpect(status().isUnauthorized());
    }
}

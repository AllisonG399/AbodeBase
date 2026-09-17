package com.abodebase.api.auth.controller;

import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ============================================
    // Unauthenticated User
    // ============================================

    @Test
    void unauthenticatedUserCannotAccessAdminUsers() throws Exception {

        mockMvc.perform(
            get("/api/admin/users")
        )
        .andExpect(status().isUnauthorized());
    }

    // ============================================
    // Regular USER Cannot Access Admin Endpoint
    // ============================================

    @Test
    void regularUserCannotAccessAdminUsers() throws Exception {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        String username =
            "adminuser" + timestamp;

        String email =
            "adminuser" + timestamp + "@example.com";

        String password =
            "Password123!";

        // Register USER
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

        // Login USER
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

        // Attempt Admin Endpoint
        mockMvc.perform(
            get("/api/admin/users")
                .session(session)
        )
        .andExpect(status().isForbidden());
    }

    // ============================================
    // ADMIN Can Access Admin Endpoint
    // ============================================

    @Test
    void adminUserCanAccessAdminUsers() throws Exception {

        String email =
            System.getenv("ABODEBASE_ADMIN_EMAIL");

        String password =
            System.getenv("ABODEBASE_ADMIN_PASSWORD");

        // Login Admin
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

        // Access Admin Endpoint
        mockMvc.perform(
            get("/api/admin/users")
                .session(session)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").exists())
        .andExpect(jsonPath("$[0].username").exists())
        .andExpect(jsonPath("$[0].email").exists())
        .andExpect(jsonPath("$[0].enabled").exists())
        .andExpect(jsonPath("$[0].roles").exists())
        .andExpect(jsonPath("$[0].createdAt").exists())
        .andExpect(jsonPath("$[0].updatedAt").exists())
        .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }
}
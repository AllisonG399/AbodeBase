package com.abodebase.api.auth.controller;

import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

/**
 * Tests Include:
 * Unauthenticated User
 * Regular USER Cannot Access Admin Enpoint
 * ADMIN Can Access Admin Endpoint
 * ADMIN Can Get User By ID
 * ADMIN Cannot Get Nonexistent User
 * Regular USER Cannot Get User By ID
 * Admin Can Disable a User
 * Admin Can Re-Enable a User
 * Regular User Cannot Change Status
 * Missing Enabled Value
 * Nonexistent User
 */
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

    // ============================================
    // ADMIN Can Get User By ID
    // ============================================

    @Test
    void adminUserCanGetUserById() throws Exception {

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

        // Get all users
        String response =
            mockMvc.perform(
                get("/api/admin/users")
                    .session(session)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        // Extract first user ID
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode users = objectMapper.readTree(response);

        String userId =
            users.get(0).get("id").asString();

        // Get user by ID
        mockMvc.perform(
            get("/api/admin/users/{id}", userId)
                .session(session)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId))
        .andExpect(jsonPath("$.username").exists())
        .andExpect(jsonPath("$.email").exists())
        .andExpect(jsonPath("$.enabled").exists())
        .andExpect(jsonPath("$.roles").exists())
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    // ============================================
    // ADMIN Cannot Get Nonexistent User
    // ============================================

    @Test
    void adminUserCannotGetNonexistentUser() throws Exception {

        String email =
            System.getenv("ABODEBASE_ADMIN_EMAIL");

        String password =
            System.getenv("ABODEBASE_ADMIN_PASSWORD");

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
            (MockHttpSession) mockMvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession();

        mockMvc.perform(
            get("/api/admin/users/{id}",
                "00000000-0000-0000-0000-000000000000"
            )
            .session(session)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Regular USER Cannot Get User By ID
    // ============================================

    @Test
    void regularUserCannotGetUserById() throws Exception {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        String username =
            "lookupuser" + timestamp;

        String email =
            "lookupuser" + timestamp + "@example.com";

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
            (MockHttpSession) mockMvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession();

        // Attempt Admin Endpoint
        mockMvc.perform(
            get(
                "/api/admin/users/{id}",
                "00000000-0000-0000-0000-000000000000"
            )
            .session(session)
        )
        .andExpect(status().isForbidden());
    }

    // ============================================
    // Admin can disable a user
    // ============================================

   @Test
    void adminCanDisableAUser() throws Exception {

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
            (MockHttpSession) mockMvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession();

        // Get all users so we have an existing user ID
        String response =
            mockMvc.perform(
                get("/api/admin/users")
                    .session(session)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode users =
            objectMapper.readTree(response);

        String userId = null;

        for (JsonNode user : users) {

            boolean isAdmin = false;

            for (JsonNode role : user.get("roles")) {
                if ("ADMIN".equals(role.asString())) {
                    isAdmin = true;
                    break;
                }
            }

            if (!isAdmin) {
                userId = user.get("id").asString();
                break;
            }
        }

        // Disable the user
        mockMvc.perform(
            patch("/api/admin/users/" + userId + "/status")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "enabled": false
                    }
                    """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.enabled").value(false));
    }

    // ============================================
    // Admin can re-enable a user
    // ============================================

    @Test
    void adminCanReEnableAUser() throws Exception {

        String adminEmail =
            System.getenv("ABODEBASE_ADMIN_EMAIL");

        String adminPassword =
            System.getenv("ABODEBASE_ADMIN_PASSWORD");

        // Login Admin
        String loginBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(
            adminEmail,
            adminPassword
        );

        MockHttpSession session =
            (MockHttpSession) mockMvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession();

        // Get all users and find a regular user
        String response =
            mockMvc.perform(
                get("/api/admin/users")
                    .session(session)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode users =
            objectMapper.readTree(response);

        String userId = null;

        for (JsonNode user : users) {

            boolean isAdmin = false;

            for (JsonNode role : user.get("roles")) {
                if ("ADMIN".equals(role.asString())) {
                    isAdmin = true;
                    break;
                }
            }

            if (!isAdmin) {
                userId = user.get("id").asString();
                break;
            }
        }

        // Disable the user first
        mockMvc.perform(
            patch("/api/admin/users/" + userId + "/status")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "enabled": false
                    }
                    """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.enabled").value(false));

        // Re-enable the user
        mockMvc.perform(
            patch("/api/admin/users/" + userId + "/status")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "enabled": true
                    }
                    """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.enabled").value(true));
    }

    // ============================================
    // Regular User cannot change status
    // ============================================

    @Test
    void regularUserCannotChangeStatus() throws Exception {

        String uniqueId =
            String.valueOf(System.currentTimeMillis());

        String username =
            "statustest" + uniqueId;

        String email =
            "statustest" + uniqueId + "@example.com";

        String password = "TestPassword123!";

        // Register regular user
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

        // Login regular user
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
            (MockHttpSession) mockMvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession();

        // Attempt to change user status
        mockMvc.perform(
            patch("/api/admin/users/00000000-0000-0000-0000-000000000000/status")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "enabled": false
                    }
                    """)
        )
        .andExpect(status().isForbidden());
    }

    // ============================================
    // Missing enabled value
    // ============================================

    @Test
    void missingEnabledValue() throws Exception {

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
            (MockHttpSession) mockMvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession();

        // Attempt to update status without providing enabled
        mockMvc.perform(
            patch("/api/admin/users/00000000-0000-0000-0000-000000000001/status")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {}
                    """)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Nonexistent user
    // ============================================

    @Test
    void nonexistentUser() throws Exception {

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
            (MockHttpSession) mockMvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession();

        // Attempt to update a user that does not exist
        mockMvc.perform(
            patch("/api/admin/users/00000000-0000-0000-0000-000000000000/status")
                .session(session)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "enabled": false
                    }
                    """)
        )
        .andExpect(status().isBadRequest());
    }


}
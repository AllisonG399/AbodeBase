package com.abodebase.api.auth;

import com.abodebase.api.auth.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;


    // ============================================
    // Successful Login
    // ============================================

    @Test
    void loginWithValidCredentialsReturnsOk() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setIdentifier(System.getenv("ABODEBASE_ADMIN_EMAIL"));
        request.setPassword(System.getenv("ABODEBASE_ADMIN_PASSWORD"));

        String requestBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(
            request.getIdentifier(),
            request.getPassword()
        );

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk());
    }

    // ============================================
    // Authenticated Session
    // ============================================

    @Test
    void authenticatedUserCanAccessCurrentUserEndpoint() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setIdentifier(System.getenv("ABODEBASE_ADMIN_EMAIL"));
        request.setPassword(System.getenv("ABODEBASE_ADMIN_PASSWORD"));

        String requestBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(
            request.getIdentifier(),
            request.getPassword()
        );

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/auth/me")
                .session(session)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email")
            .value(System.getenv("ABODEBASE_ADMIN_EMAIL").toLowerCase()))
        .andExpect(jsonPath("$.roles",
            org.hamcrest.Matchers.hasItem("ADMIN")));
    }

    // ============================================
    // Unauthenticated User
    // ============================================

    @Test
    void unauthenticatedUserCannotAccessCurrentUserEndpoint() throws Exception {

        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/auth/me")
        )
        .andExpect(status().isUnauthorized());
    }

    // ============================================
    // Invalid Password
    // ============================================

    @Test
    void loginWithInvalidPasswordReturnsUnauthorized() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setIdentifier(System.getenv("ABODEBASE_ADMIN_EMAIL"));
        request.setPassword("DefinitelyWrongPassword123!");

        String requestBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(
                request.getIdentifier(),
                request.getPassword()
            );

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isUnauthorized());
    }

    // ============================================ 
    // Invalid Account 
    // ============================================ 

    @Test 
    void loginWithInvalidAccountReturnsUnauthorized() throws Exception { 

        LoginRequest request = new LoginRequest(); 

        request.setIdentifier("nonexistent@abodebase.com"); 
        request.setPassword("ValidLookingPassword123!"); 
        
        String requestBody = """ 
            { 
                "identifier": "%s", 
                "password": "%s" 
            } 
            """.formatted( 
                request.getIdentifier(), 
                request.getPassword() 
            ); 
        
        mockMvc.perform( 
            post("/api/auth/login") 
                .with(csrf()) 
                .contentType(MediaType.APPLICATION_JSON) 
                .content(requestBody) 
        ) 
        .andExpect(status().isUnauthorized()); 
    }

    // ============================================ 
    // Missing Identifier
    // ============================================ 

    @Test
    void loginWithMissingIdentifierReturnsBadRequest() throws Exception {

        String requestBody = """
            {
                "password": "%s"
            }
            """.formatted(
            System.getenv("ABODEBASE_ADMIN_PASSWORD")
        );

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================ 
    // Missing Password
    // ============================================ 

    @Test
    void loginWithMissingPasswordReturnsBadRequest() throws Exception {

        String requestBody = """
            {
                "identifier": "%s"
            }
            """.formatted(
            System.getenv("ABODEBASE_ADMIN_EMAIL")
        );

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================ 
    // Empty Identifier
    // ============================================ 

    @Test
    void loginWithEmptyIdentifierReturnsBadRequest() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setIdentifier("");
        request.setPassword(System.getenv("ABODEBASE_ADMIN_PASSWORD"));

        String requestBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }    
            """.formatted(
                request.getIdentifier(),
                request.getPassword()
            );

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================ 
    // Empty Password
    // ============================================ 

    @Test
    void loginWithEmptyPasswordReturnsBadRequest() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setIdentifier(System.getenv("ABODEBASE_ADMIN_EMAIL"));
        request.setPassword("");

        String requestBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }    
            """.formatted(
                request.getIdentifier(),
                request.getPassword()
            );

        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Authenticated User Can Log Out
    // ============================================

    @Test
    void authenticatedUserCanLogout() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setIdentifier(System.getenv("ABODEBASE_ADMIN_EMAIL"));
        request.setPassword(System.getenv("ABODEBASE_ADMIN_PASSWORD"));

        String requestBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(
            request.getIdentifier(),
            request.getPassword()
        );

        MockHttpSession session = new MockHttpSession();

        // Login
        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk());

        // Logout
        mockMvc.perform(
            post("/api/auth/logout")
                .with(csrf())
                .session(session)
        )
        .andExpect(status().isOk());
    }

    // ============================================
    // Logged Out User Cannot Access Current User Endpoint
    // ============================================
    @Test
    void loggedOutUserCannotAccessCurrentUserEndpoint() throws Exception {

        LoginRequest request = new LoginRequest();

        request.setIdentifier(System.getenv("ABODEBASE_ADMIN_EMAIL"));
        request.setPassword(System.getenv("ABODEBASE_ADMIN_PASSWORD"));

        String requestBody = """
            {
                "identifier": "%s",
                "password": "%s"
            }
            """.formatted(
            request.getIdentifier(),
            request.getPassword()
        );

        MockHttpSession session = new MockHttpSession();

        // Login
        mockMvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isOk());

        // Verify authentication exists
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/auth/me")
                .session(session)
        )
        .andExpect(status().isOk());

        // Logout
        mockMvc.perform(
            post("/api/auth/logout")
                .with(csrf())
                .session(session)
        )
        .andExpect(status().isOk());

        // Verify authentication is gone
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/auth/me")
                .session(session)
        )
        .andExpect(status().isUnauthorized());
    }

    // ============================================
    // Unauthorized logout
    // ============================================

    @Test
    void unauthenticatedUserCanLogout() throws Exception {

        mockMvc.perform(
            post("/api/auth/logout")
                .with(csrf())
        )
        .andExpect(status().isOk());
    }
}
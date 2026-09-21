package com.abodebase.api.auth;

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

/**
 * Tests Include:
 * Successful Login
 * Authenticated Session
 * Unauthenticated User
 * Invalid Password
 * Invalid Account
 * Missing Identifier
 * Missing Password
 * Empty Identifier
 * Empty Password
 * Authenticated User Can Logout
 * Logged Out User Cannot Access Current User Endpoint
 * Unauthorized Logout
 * Successful Registration
 * Missing Registration Username
 * Missing Registration Email
 * Invalid Registration Email
 * Missing Registration Password
 * Short Registration Password
 * Long Registration Password
 * Registration Password Without Letter
 * Registration Password Without Number
 * Invalid Registration Username
 * Reserved Registration Username
 * Blocked Registration Username
 * Duplicate Username
 * Duplicate Email
 * Registration --> Login --> Current User
 */
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

    // ============================================
    // Successful Registration
    // ============================================

    @Test
    void registrationWithValidCredentialsReturnsCreated() throws Exception {

        String username =
            "testuser" + System.currentTimeMillis();

        String email =
            "test" + System.currentTimeMillis() + "@example.com";

        RegisterRequest request = new RegisterRequest();

        request.setUsername(username);
        request.setEmail(email);
        request.setPassword("Password123!");

        String requestBody = """
            {
                "username": "%s",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(
            request.getUsername(),
            request.getEmail(),
            request.getPassword()
        );

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isCreated());
    }

    // ============================================
    // Missing Registration Username
    // ============================================

    @Test
    void registrationWithMissingUsernameReturnsBadRequest() throws Exception {

        String requestBody = """
            {
                "email": "newuser@example.com",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Missing Registration Email
    // ============================================

    @Test
    void registrationWithMissingEmailReturnsBadRequest() throws Exception {

        String requestBody = """
            {
                "username": "newuser123",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Invalid Registration Email
    // ============================================

    @Test
    void registrationWithInvalidEmailReturnsBadRequest() throws Exception {

        String requestBody = """
            {
                "username": "newuser123",
                "email": "not-an-email",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Missing Registration Password
    // ============================================

    @Test
    void registrationWithMissingPasswordReturnsBadRequest() throws Exception {

        String requestBody = """
            {
                "username": "newuser123",
                "email": "newuser@example.com"
            }
            """;

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Short Registration Password
    // ============================================

    @Test
    void registrationWithShortPasswordReturnsBadRequest() throws Exception {

        String requestBody = """
            {
                "username": "newuser123",
                "email": "newuser@example.com",
                "password": "short"
            }
            """;

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Long Registration Password
    // ============================================
    @Test
    void longRegistrationPassword() throws Exception {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        String username =
            "longpassword" + timestamp;

        String email =
            "longpassword" + timestamp + "@example.com";

        String password =
            "A1" + "a".repeat(71);

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
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Registration Password Without Letter
    // ============================================
    @Test
    void registrationPasswordWithoutLetter() throws Exception {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        String username =
            "nonletter" + timestamp;

        String email =
            "nonletter" + timestamp + "@example.com";

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "%s",
                        "email": "%s",
                        "password": "12345678"
                    }
                    """.formatted(
                    username,
                    email
                ))
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Registration Password Without Number
    // ============================================
    @Test
    void registrationPasswordWithoutNumber() throws Exception {

        String timestamp =
            String.valueOf(System.currentTimeMillis());

        String username =
            "nonnumber" + timestamp;

        String email =
            "nonnumber" + timestamp + "@example.com";

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "%s",
                        "email": "%s",
                        "password": "abcdefgh"
                    }
                    """.formatted(
                    username,
                    email
                ))
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Invalid Registration Username
    // ============================================

    @Test
    void registrationWithInvalidUsernameReturnsBadRequest() throws Exception {

        String requestBody = """
            {
                "username": "bad_name",
                "email": "newuser@example.com",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Reserved Registration Username
    // ============================================

    @Test
    void registrationWithReservedUsernameReturnsBadRequest() throws Exception {

        String requestBody = """
            {
                "username": "admin",
                "email": "newuser@example.com",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Blocked Registration Username
    // ============================================

    @Test
    void registrationWithBlockedUsernameReturnsBadRequest() throws Exception {

        String requestBody = """
            {
                "username": "blockeduser",
                "email": "newuser@example.com",
                "password": "Password123!"
            }
            """;

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Duplicate Username
    // ============================================

    @Test
    void registrationWithDuplicateUsernameIsRejected() throws Exception {

        String username =
            "duplicate" + System.currentTimeMillis();

        String firstEmail =
            "first" + System.currentTimeMillis() + "@example.com";

        String secondEmail =
            "second" + System.currentTimeMillis() + "@example.com";

        String firstRequest = """
            {
                "username": "%s",
                "email": "%s",
                "password": "Password123!"
            }
            """.formatted(username, firstEmail);

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequest)
        )
        .andExpect(status().isCreated());

        String secondRequest = """
            {
                "username": "%s",
                "email": "%s",
                "password": "Password123!"
            }
            """.formatted(username, secondEmail);

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondRequest)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Duplicate Email
    // ============================================

    @Test
    void registrationWithDuplicateEmailIsRejected() throws Exception {

        String firstUsername =
            "firstuser" + System.currentTimeMillis();

        String secondUsername =
            "seconduser" + System.currentTimeMillis();

        String email =
            "duplicate" + System.currentTimeMillis() + "@example.com";

        String firstRequest = """
            {
                "username": "%s",
                "email": "%s",
                "password": "Password123!"
            }
            """.formatted(firstUsername, email);

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequest)
        )
        .andExpect(status().isCreated());

        String secondRequest = """
            {
                "username": "%s",
                "email": "%s",
                "password": "Password123!"
            }
            """.formatted(secondUsername, email);

        mockMvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(secondRequest)
        )
        .andExpect(status().isBadRequest());
    }

    // ============================================
    // Registration → Login → Current User
    // ============================================

    @Test
    void registeredUserCanLoginAndAccessCurrentUser() throws Exception {

        String timestamp = String.valueOf(System.currentTimeMillis());

        String username = "flowuser" + timestamp;
        String email = "flow" + timestamp + "@example.com";
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

        // Log in
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

        // Access current user
        mockMvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/auth/me")
                .session(session)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(
            jsonPath("$.roles",
                org.hamcrest.Matchers.hasItem("USER"))
        );
    }
}
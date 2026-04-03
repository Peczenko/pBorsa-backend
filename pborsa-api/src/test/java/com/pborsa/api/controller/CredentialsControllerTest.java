package com.pborsa.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pborsa.api.config.TestSecurityConfig;
import com.pborsa.api.credentials.CredentialsController;
import com.pborsa.domain.dto.credentials.CredentialsRegistrationRequest;
import com.pborsa.api.security.WithMockFirebaseUser;
import com.pborsa.trading.credentials.UserCredentialsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for {@link CredentialsController}.
 * <p>
 * Tests controller behavior with mocked service dependencies.
 */
@WebMvcTest(CredentialsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("CredentialsController")
class CredentialsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserCredentialsService credentialsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("POST /api/v1/credentials/{userId}")
    class RegisterCredentials {

        @Test
        @DisplayName("should register credentials successfully")
        @WithMockFirebaseUser(uid = "cred-test-uid-9230", email = "cred9230@test.com")
        void registersCredentialsSuccessfully() throws Exception {
            // given
            Long userId = 9230L;
            CredentialsRegistrationRequest request = new CredentialsRegistrationRequest(
                    "AKIAIOSFODNN7EXAMPLE",
                    "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
                    false
            );

            when(credentialsService.registerCredentials(eq(userId), any(CredentialsRegistrationRequest.class)))
                    .thenReturn(true);

            // when/then
            mockMvc.perform(post("/api/v1/credentials/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(true)))
                    .andExpect(jsonPath("$.message", is("Credentials registered successfully")));
        }

        @Test
        @DisplayName("should return bad request when credentials verification fails")
        @WithMockFirebaseUser(uid = "cred-test-uid-9231", email = "cred9231@test.com")
        void returnsBadRequestWhenVerificationFails() throws Exception {
            // given
            Long userId = 9231L;
            CredentialsRegistrationRequest request = new CredentialsRegistrationRequest(
                    "invalid-key",
                    "invalid-secret",
                    false
            );

            when(credentialsService.registerCredentials(eq(userId), any(CredentialsRegistrationRequest.class)))
                    .thenReturn(false);

            // when/then
            mockMvc.perform(post("/api/v1/credentials/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success", is(false)))
                    .andExpect(jsonPath("$.error", is("Invalid credentials - verification failed")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/credentials/{userId}/status")
    class HasCredentials {

        @Test
        @DisplayName("should return true when credentials exist")
        @WithMockFirebaseUser(uid = "cred-test-uid-9233", email = "cred9233@test.com")
        void returnsTrueWhenCredentialsExist() throws Exception {
            // given
            Long userId = 9233L;
            when(credentialsService.hasCredentials(userId)).thenReturn(true);

            // when/then
            mockMvc.perform(get("/api/v1/credentials/{userId}/status", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(true)));
        }

        @Test
        @DisplayName("should return false when credentials do not exist")
        @WithMockFirebaseUser(uid = "cred-test-uid-9234", email = "cred9234@test.com")
        void returnsFalseWhenCredentialsDoNotExist() throws Exception {
            // given
            Long userId = 9234L;
            when(credentialsService.hasCredentials(userId)).thenReturn(false);

            // when/then
            mockMvc.perform(get("/api/v1/credentials/{userId}/status", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(false)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/credentials/{userId}")
    class DeactivateCredentials {

        @Test
        @DisplayName("should deactivate credentials successfully")
        @WithMockFirebaseUser(uid = "cred-test-uid-9235", email = "cred9235@test.com")
        void deactivatesCredentialsSuccessfully() throws Exception {
            // given
            Long userId = 9235L;
            doNothing().when(credentialsService).deactivateCredentials(userId);

            // when/then
            mockMvc.perform(delete("/api/v1/credentials/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Credentials deactivated successfully")));

            verify(credentialsService).deactivateCredentials(userId);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/credentials/{userId}/refresh")
    class RefreshCredentials {

        @Test
        @DisplayName("should refresh credentials cache successfully")
        @WithMockFirebaseUser(uid = "cred-test-uid-9236", email = "cred9236@test.com")
        void refreshesCredentialsCacheSuccessfully() throws Exception {
            // given
            Long userId = 9236L;

            // when/then
            mockMvc.perform(post("/api/v1/credentials/{userId}/refresh", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", is("Credentials cache refreshed")));

            verify(credentialsService).refreshCredentials(userId);
        }
    }
}

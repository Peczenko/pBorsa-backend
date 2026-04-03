package com.pborsa.api.controller;

import com.pborsa.api.config.TestSecurityConfig;
import com.pborsa.api.user.UserController;
import com.pborsa.domain.dto.user.UserProfileDto;
import com.pborsa.domain.dto.user.UserStatus;
import com.pborsa.api.shared.security.FirebaseUserPrincipal;
import com.pborsa.api.security.WithMockFirebaseUser;
import com.pborsa.api.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc tests for {@link UserController}.
 * <p>
 * Tests controller behavior with mocked service dependencies.
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("GET /api/v1/users/me")
    class GetCurrentUser {

        @Test
        @DisplayName("should return user profile when authenticated")
        @WithMockFirebaseUser(uid = "test-uid-9200", email = "user9200@test.com")
        void returnsUserProfileWhenAuthenticated() throws Exception {
            // given
            Instant now = Instant.now();
            UserProfileDto profile = new UserProfileDto(
                    9200L,
                    "test-uid-9200",
                    "user9200@test.com",
                    "Test User",
                    "google.com",
                    UserStatus.ACTIVE,
                    now,
                    now
            );
            when(userService.syncUserProfile(any(FirebaseUserPrincipal.class))).thenReturn(profile);

            // when/then
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(9200)))
                    .andExpect(jsonPath("$.data.firebaseUid", is("test-uid-9200")))
                    .andExpect(jsonPath("$.data.email", is("user9200@test.com")));
        }

        @Test
        @DisplayName("should return admin user profile")
        @WithMockFirebaseUser(uid = "admin-uid-9201", email = "admin9201@test.com", admin = true)
        void returnsAdminUserProfile() throws Exception {
            // given
            Instant now = Instant.now();
            UserProfileDto profile = new UserProfileDto(
                    9201L,
                    "admin-uid-9201",
                    "admin9201@test.com",
                    "Admin User",
                    "google.com",
                    UserStatus.ACTIVE,
                    now,
                    now
            );
            when(userService.syncUserProfile(any(FirebaseUserPrincipal.class))).thenReturn(profile);

            // when/then
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.status", is("ACTIVE")));
        }

        @Test
        @DisplayName("should handle user with minimal profile")
        @WithMockFirebaseUser(uid = "minimal-uid-9202", email = "minimal@test.com")
        void handlesMinimalProfile() throws Exception {
            // given
            Instant now = Instant.now();
            UserProfileDto profile = new UserProfileDto(
                    9202L,
                    "minimal-uid-9202",
                    "minimal@test.com",
                    null, // no display name
                    "password",
                    UserStatus.ACTIVE,
                    now,
                    now
            );
            when(userService.syncUserProfile(any(FirebaseUserPrincipal.class))).thenReturn(profile);

            // when/then
            mockMvc.perform(get("/api/v1/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.email", is("minimal@test.com")));
        }
    }
}

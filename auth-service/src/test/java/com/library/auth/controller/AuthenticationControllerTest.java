package com.library.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.auth.BaseIntegrationTest;
import com.library.auth.dto.AuthenticationRequest;
import com.library.auth.dto.RegisterRequest;
import com.library.auth.entity.Role;
import com.library.auth.entity.User;
import com.library.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class AuthenticationControllerTest extends BaseIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();

                // Create a default user for login tests
                User user = User.builder()
                                .firstname("Test")
                                .lastname("User")
                                .email("test@user.com")
                                .password(passwordEncoder.encode("password"))
                                .role(Role.USER)
                                .build();
                userRepository.save(user);
        }

        @Test
        void shouldRegisterUserSuccessfully() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .firstname("John")
                                .lastname("Doe")
                                .email("john.doe@example.com")
                                .password("SecurePass123!") // Valid password
                                .build();

                mockMvc.perform(post("/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists());
        }

        @Test
        void shouldLoginUserSuccessfully() throws Exception {
                AuthenticationRequest request = AuthenticationRequest.builder()
                                .email("test@user.com")
                                .password("password")
                                .build();

                mockMvc.perform(post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists());
        }

        @Test
        void shouldFailLoginWithWrongPassword() throws Exception {
                AuthenticationRequest request = AuthenticationRequest.builder()
                                .email("test@user.com")
                                .password("wrongpassword")
                                .build();

                mockMvc.perform(post("/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized()); // 401
        }

        @Test
        void shouldFailRegistrationWithDuplicateEmail() throws Exception {
                RegisterRequest request = RegisterRequest.builder()
                                .firstname("Duplicate")
                                .lastname("User")
                                .email("test@user.com") // Duplicate
                                .password("SecurePass123!")
                                .build();

                mockMvc.perform(post("/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict()); // 409
        }
}

package com.eduapi.controller;

import com.eduapi.dto.LoginDTO;
import com.eduapi.dto.RegisterDTO;
import com.eduapi.entity.Role;
import com.eduapi.entity.User;
import com.eduapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register creates user and returns token")
    void register_success() throws Exception {
        RegisterDTO dto = RegisterDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register fails on duplicate username")
    void register_duplicateUsername() throws Exception {
        // Pre-create user
        userRepository.save(User.builder()
                .username("testuser").email("existing@example.com")
                .password(passwordEncoder.encode("pass"))
                .role(Role.STUDENT).firstName("X").lastName("Y")
                .build());

        RegisterDTO dto = RegisterDTO.builder()
                .username("testuser")
                .email("new@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already taken")));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/login returns token for valid credentials")
    void login_success() throws Exception {
        // Pre-create user
        userRepository.save(User.builder()
                .username("loginuser").email("login@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.STUDENT).firstName("Login").lastName("User")
                .build());

        LoginDTO dto = LoginDTO.builder()
                .username("loginuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("loginuser"));
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/login fails for invalid password")
    void login_badPassword() throws Exception {
        userRepository.save(User.builder()
                .username("loginuser2").email("login2@example.com")
                .password(passwordEncoder.encode("correct"))
                .role(Role.STUDENT).firstName("X").lastName("Y")
                .build());

        LoginDTO dto = LoginDTO.builder()
                .username("loginuser2")
                .password("wrong")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid")));
    }

    @Test
    @Order(5)
    @DisplayName("Protected endpoint returns 401 without token")
    void protectedEndpoint_noToken() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(6)
    @DisplayName("Protected endpoint succeeds with valid token")
    void protectedEndpoint_withToken() throws Exception {
        // Register and get token
        RegisterDTO dto = RegisterDTO.builder()
                .username("tokenuser").email("token@example.com")
                .password("password123").firstName("Token").lastName("User")
                .role("ADMIN")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(
                result.getResponse().getContentAsString()).get("token").asText();

        // Use token to access protected endpoint
        mockMvc.perform(get("/api/students")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    @DisplayName("POST /api/auth/register fails with validation errors")
    void register_validationFailure() throws Exception {
        RegisterDTO dto = RegisterDTO.builder()
                .username("")  // blank — should fail
                .email("not-an-email")  // invalid
                .password("12")  // too short
                .firstName("")
                .lastName("")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}

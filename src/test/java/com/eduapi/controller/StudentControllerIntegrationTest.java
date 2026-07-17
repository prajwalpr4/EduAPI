package com.eduapi.controller;

import com.eduapi.dto.RegisterDTO;
import com.eduapi.dto.StudentCreateDTO;
import com.eduapi.entity.Role;
import com.eduapi.entity.User;
import com.eduapi.repository.StudentRepository;
import com.eduapi.repository.UserRepository;
import com.eduapi.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private StudentRepository studentRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp() {
        studentRepository.deleteAll();
        userRepository.deleteAll();

        // Create admin user
        User admin = userRepository.save(User.builder()
                .username("admin").email("admin@example.com")
                .password(passwordEncoder.encode("adminpass"))
                .role(Role.ADMIN).firstName("Admin").lastName("User")
                .build());

        adminToken = jwtUtil.generateToken("admin", "ADMIN");
    }

    @Test
    @Order(1)
    @DisplayName("GET /api/students returns empty page initially")
    void getAllStudents_empty() throws Exception {
        mockMvc.perform(get("/api/students")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/students creates a student")
    void createStudent() throws Exception {
        StudentCreateDTO dto = StudentCreateDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .username("janesmith")
                .password("password123")
                .enrollmentDate(LocalDate.of(2024, 9, 1))
                .dateOfBirth(LocalDate.of(2002, 3, 15))
                .build();

        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.studentCode").isNotEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/students with invalid data returns validation error")
    void createStudent_validationError() throws Exception {
        StudentCreateDTO dto = StudentCreateDTO.builder()
                .firstName("")  // blank
                .lastName("")  // blank
                .email("bad-email")  // invalid
                .username("ab")  // too short
                .password("12")  // too short
                .enrollmentDate(LocalDate.of(2024, 9, 1))
                .build();

        mockMvc.perform(post("/api/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/students/{id} returns 404 for non-existent student")
    void getStudent_notFound() throws Exception {
        mockMvc.perform(get("/api/students/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Student")));
    }

    @Test
    @Order(5)
    @DisplayName("Unauthenticated request returns 403")
    void noAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/students"))
                .andExpect(status().isForbidden());
    }
}

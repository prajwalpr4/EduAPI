package com.eduapi.service;

import com.eduapi.dto.*;
import com.eduapi.entity.*;
import com.eduapi.exception.ResourceNotFoundException;
import com.eduapi.repository.StudentRepository;
import com.eduapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private StudentService studentService;

    private User testUser;
    private Student testStudent;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .password("encoded")
                .role(Role.STUDENT)
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testStudent = Student.builder()
                .id(1L)
                .user(testUser)
                .studentCode("STU-ABCD1234")
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .enrollmentDate(LocalDate.of(2024, 9, 1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllStudents returns paginated DTOs")
    void getAllStudents() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = new PageImpl<>(List.of(testStudent), pageable, 1);
        when(studentRepository.findAll(pageable)).thenReturn(page);

        Page<StudentDTO> result = studentService.getAllStudents(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
        assertThat(result.getContent().get(0).getStudentCode()).isEqualTo("STU-ABCD1234");
        verify(studentRepository).findAll(pageable);
    }

    @Test
    @DisplayName("getStudentById returns DTO when found")
    void getStudentById_found() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        StudentDTO result = studentService.getStudentById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("getStudentById throws when not found")
    void getStudentById_notFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student");
    }

    @Test
    @DisplayName("createStudent creates user and student records")
    void createStudent() {
        StudentCreateDTO dto = StudentCreateDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .username("janesmith")
                .password("password123")
                .enrollmentDate(LocalDate.of(2024, 9, 1))
                .build();

        when(userRepository.existsByUsername("janesmith")).thenReturn(false);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            return u;
        });
        when(studentRepository.save(any(Student.class))).thenAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setId(2L);
            s.setCreatedAt(LocalDateTime.now());
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        });

        StudentDTO result = studentService.createStudent(dto);

        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getStudentCode()).startsWith("STU-");
        verify(userRepository).save(any(User.class));
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    @DisplayName("createStudent throws on duplicate username")
    void createStudent_duplicateUsername() {
        StudentCreateDTO dto = StudentCreateDTO.builder()
                .username("johndoe")
                .email("new@example.com")
                .password("pass")
                .firstName("X")
                .lastName("Y")
                .enrollmentDate(LocalDate.now())
                .build();

        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    @DisplayName("updateStudent updates user and student fields")
    void updateStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        StudentUpdateDTO dto = StudentUpdateDTO.builder()
                .firstName("Johnny")
                .lastName("Doe")
                .email("newemail@example.com")
                .phone("9876543210")
                .build();

        StudentDTO result = studentService.updateStudent(1L, dto);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("deleteStudent removes student and user")
    void deleteStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        studentService.deleteStudent(1L);

        verify(studentRepository).delete(testStudent);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("countStudents returns repository count")
    void countStudents() {
        when(studentRepository.count()).thenReturn(42L);

        assertThat(studentService.countStudents()).isEqualTo(42L);
    }

    @Test
    @DisplayName("searchStudents delegates to repository search")
    void searchStudents() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = new PageImpl<>(List.of(testStudent), pageable, 1);
        when(studentRepository.searchStudents("John", pageable)).thenReturn(page);

        Page<StudentDTO> result = studentService.searchStudents("John", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(studentRepository).searchStudents("John", pageable);
    }

    @Test
    @DisplayName("searchStudents with blank search falls back to findAll")
    void searchStudents_blankSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Student> page = new PageImpl<>(List.of(testStudent), pageable, 1);
        when(studentRepository.findAll(pageable)).thenReturn(page);

        Page<StudentDTO> result = studentService.searchStudents("", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(studentRepository).findAll(pageable);
    }
}

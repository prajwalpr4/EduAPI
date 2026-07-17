package com.eduapi.service;

import com.eduapi.dto.*;
import com.eduapi.entity.*;
import com.eduapi.exception.ResourceNotFoundException;
import com.eduapi.repository.CourseRepository;
import com.eduapi.repository.EnrollmentRepository;
import com.eduapi.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CourseRepository courseRepository;

    @InjectMocks private EnrollmentService enrollmentService;

    private User testUser;
    private Student testStudent;
    private Course testCourse;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L).username("johndoe").email("john@example.com")
                .firstName("John").lastName("Doe").role(Role.STUDENT)
                .build();

        testStudent = Student.builder()
                .id(1L).user(testUser).studentCode("STU-ABCD1234")
                .enrollmentDate(LocalDate.of(2024, 9, 1))
                .build();

        testCourse = Course.builder()
                .id(1L).courseCode("CS101").title("Intro to CS")
                .credits(3).maxCapacity(30)
                .build();

        testEnrollment = Enrollment.builder()
                .id(1L).student(testStudent).course(testCourse)
                .enrollmentDate(LocalDate.of(2024, 9, 1))
                .status(EnrollmentStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllEnrollments returns DTOs")
    void getAllEnrollments() {
        when(enrollmentRepository.findAll()).thenReturn(List.of(testEnrollment));

        List<EnrollmentDTO> result = enrollmentService.getAllEnrollments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentName()).isEqualTo("John Doe");
        assertThat(result.get(0).getCourseCode()).isEqualTo("CS101");
    }

    @Test
    @DisplayName("getEnrollmentById returns DTO")
    void getEnrollmentById_found() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));

        EnrollmentDTO result = enrollmentService.getEnrollmentById(1L);

        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("getEnrollmentById throws when not found")
    void getEnrollmentById_notFound() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.getEnrollmentById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createEnrollment saves enrollment")
    void createEnrollment() {
        EnrollmentCreateDTO dto = EnrollmentCreateDTO.builder()
                .studentId(1L).courseId(1L).build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.countByCourseId(1L)).thenReturn(5L);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(inv -> {
            Enrollment e = inv.getArgument(0);
            e.setId(2L);
            e.setCreatedAt(LocalDateTime.now());
            e.setUpdatedAt(LocalDateTime.now());
            return e;
        });

        EnrollmentDTO result = enrollmentService.createEnrollment(dto);

        assertThat(result.getStudentName()).isEqualTo("John Doe");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("createEnrollment throws on duplicate")
    void createEnrollment_duplicate() {
        EnrollmentCreateDTO dto = EnrollmentCreateDTO.builder()
                .studentId(1L).courseId(1L).build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.createEnrollment(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already enrolled");
    }

    @Test
    @DisplayName("createEnrollment throws when course is full")
    void createEnrollment_full() {
        EnrollmentCreateDTO dto = EnrollmentCreateDTO.builder()
                .studentId(1L).courseId(1L).build();

        when(studentRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.countByCourseId(1L)).thenReturn(30L); // maxCapacity = 30

        assertThatThrownBy(() -> enrollmentService.createEnrollment(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maximum capacity");
    }

    @Test
    @DisplayName("updateEnrollment updates grade and status")
    void updateEnrollment() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        EnrollmentUpdateDTO dto = EnrollmentUpdateDTO.builder()
                .grade("A").status("COMPLETED").build();

        EnrollmentDTO result = enrollmentService.updateEnrollment(1L, dto);

        assertThat(result).isNotNull();
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    @DisplayName("deleteEnrollment removes enrollment")
    void deleteEnrollment() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));

        enrollmentService.deleteEnrollment(1L);

        verify(enrollmentRepository).delete(testEnrollment);
    }
}

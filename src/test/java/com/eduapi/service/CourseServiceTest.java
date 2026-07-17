package com.eduapi.service;

import com.eduapi.dto.*;
import com.eduapi.entity.Course;
import com.eduapi.exception.ResourceNotFoundException;
import com.eduapi.repository.CourseRepository;
import com.eduapi.repository.EnrollmentRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private EnrollmentRepository enrollmentRepository;

    @InjectMocks private CourseService courseService;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = Course.builder()
                .id(1L)
                .courseCode("CS101")
                .title("Introduction to Computer Science")
                .description("An introductory course")
                .credits(3)
                .maxCapacity(30)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllCourses returns paginated DTOs")
    void getAllCourses() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Course> page = new PageImpl<>(List.of(testCourse), pageable, 1);
        when(courseRepository.findAll(pageable)).thenReturn(page);
        when(enrollmentRepository.countByCourseId(1L)).thenReturn(5L);

        Page<CourseDTO> result = courseService.getAllCourses(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCourseCode()).isEqualTo("CS101");
        assertThat(result.getContent().get(0).getEnrolledCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("getCourseById returns DTO when found")
    void getCourseById_found() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.countByCourseId(1L)).thenReturn(10L);

        CourseDTO result = courseService.getCourseById(1L);

        assertThat(result.getTitle()).isEqualTo("Introduction to Computer Science");
        assertThat(result.getEnrolledCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("getCourseById throws when not found")
    void getCourseById_notFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createCourse saves and returns DTO")
    void createCourse() {
        CourseCreateDTO dto = CourseCreateDTO.builder()
                .courseCode("CS102")
                .title("Data Structures")
                .credits(4)
                .maxCapacity(25)
                .build();

        when(courseRepository.existsByCourseCode("CS102")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> {
            Course c = inv.getArgument(0);
            c.setId(2L);
            c.setCreatedAt(LocalDateTime.now());
            c.setUpdatedAt(LocalDateTime.now());
            return c;
        });
        when(enrollmentRepository.countByCourseId(any())).thenReturn(0L);

        CourseDTO result = courseService.createCourse(dto);

        assertThat(result.getCourseCode()).isEqualTo("CS102");
        assertThat(result.getTitle()).isEqualTo("Data Structures");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("createCourse throws on duplicate course code")
    void createCourse_duplicateCode() {
        CourseCreateDTO dto = CourseCreateDTO.builder()
                .courseCode("CS101")
                .title("Duplicate")
                .credits(3)
                .maxCapacity(30)
                .build();

        when(courseRepository.existsByCourseCode("CS101")).thenReturn(true);

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("updateCourse updates fields")
    void updateCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        when(enrollmentRepository.countByCourseId(1L)).thenReturn(0L);

        CourseUpdateDTO dto = CourseUpdateDTO.builder()
                .title("Advanced CS")
                .credits(4)
                .maxCapacity(40)
                .build();

        CourseDTO result = courseService.updateCourse(1L, dto);

        assertThat(result).isNotNull();
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("deleteCourse removes course")
    void deleteCourse() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));

        courseService.deleteCourse(1L);

        verify(courseRepository).delete(testCourse);
    }

    @Test
    @DisplayName("countCourses returns count")
    void countCourses() {
        when(courseRepository.count()).thenReturn(15L);
        assertThat(courseService.countCourses()).isEqualTo(15L);
    }
}

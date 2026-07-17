package com.eduapi.service;

import com.eduapi.dto.*;
import com.eduapi.entity.Course;
import com.eduapi.exception.ResourceNotFoundException;
import com.eduapi.repository.CourseRepository;
import com.eduapi.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public Page<CourseDTO> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<CourseDTO> searchCourses(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return getAllCourses(pageable);
        }
        return courseRepository.searchCourses(search, pageable).map(this::toDTO);
    }

    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        return toDTO(course);
    }

    @Transactional
    public CourseDTO createCourse(CourseCreateDTO dto) {
        if (courseRepository.existsByCourseCode(dto.getCourseCode())) {
            throw new IllegalArgumentException("Course code '" + dto.getCourseCode() + "' already exists");
        }

        Course course = Course.builder()
                .courseCode(dto.getCourseCode())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .credits(dto.getCredits())
                .maxCapacity(dto.getMaxCapacity())
                .build();
        course = courseRepository.save(course);
        return toDTO(course);
    }

    @Transactional
    public CourseDTO updateCourse(Long id, CourseUpdateDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));

        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setCredits(dto.getCredits());
        course.setMaxCapacity(dto.getMaxCapacity());
        course = courseRepository.save(course);
        return toDTO(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", id));
        courseRepository.delete(course);
    }

    public long countCourses() {
        return courseRepository.count();
    }

    // ─── Mapper ───

    private CourseDTO toDTO(Course course) {
        int enrolledCount = (int) enrollmentRepository.countByCourseId(course.getId());
        return CourseDTO.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .title(course.getTitle())
                .description(course.getDescription())
                .credits(course.getCredits())
                .maxCapacity(course.getMaxCapacity())
                .enrolledCount(enrolledCount)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}

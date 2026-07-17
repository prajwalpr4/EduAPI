package com.eduapi.service;

import com.eduapi.dto.*;
import com.eduapi.entity.*;
import com.eduapi.exception.ResourceNotFoundException;
import com.eduapi.repository.CourseRepository;
import com.eduapi.repository.EnrollmentRepository;
import com.eduapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public List<EnrollmentDTO> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EnrollmentDTO getEnrollmentById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
        return toDTO(enrollment);
    }

    public List<EnrollmentDTO> getEnrollmentsByStudentId(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<EnrollmentDTO> getEnrollmentsByCourseId(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnrollmentDTO createEnrollment(EnrollmentCreateDTO dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", dto.getStudentId()));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course", "id", dto.getCourseId()));

        // Check for duplicate enrollment
        if (enrollmentRepository.existsByStudentIdAndCourseId(dto.getStudentId(), dto.getCourseId())) {
            throw new IllegalArgumentException("Student is already enrolled in this course");
        }

        // Check course capacity
        long currentEnrollment = enrollmentRepository.countByCourseId(dto.getCourseId());
        if (currentEnrollment >= course.getMaxCapacity()) {
            throw new IllegalArgumentException("Course has reached maximum capacity");
        }

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .enrollmentDate(dto.getEnrollmentDate() != null ? dto.getEnrollmentDate() : LocalDate.now())
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollment = enrollmentRepository.save(enrollment);
        return toDTO(enrollment);
    }

    @Transactional
    public EnrollmentDTO updateEnrollment(Long id, EnrollmentUpdateDTO dto) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));

        if (dto.getGrade() != null) {
            enrollment.setGrade(dto.getGrade());
        }
        if (dto.getStatus() != null) {
            enrollment.setStatus(EnrollmentStatus.valueOf(dto.getStatus()));
        }
        enrollment = enrollmentRepository.save(enrollment);
        return toDTO(enrollment);
    }

    @Transactional
    public void deleteEnrollment(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "id", id));
        enrollmentRepository.delete(enrollment);
    }

    public long countEnrollments() {
        return enrollmentRepository.count();
    }

    // ─── Mapper ───

    private EnrollmentDTO toDTO(Enrollment enrollment) {
        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();
        User user = student.getUser();

        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(student.getId())
                .studentName(user.getFirstName() + " " + user.getLastName())
                .studentCode(student.getStudentCode())
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .courseCode(course.getCourseCode())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .grade(enrollment.getGrade())
                .status(enrollment.getStatus().name())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .build();
    }
}

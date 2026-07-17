package com.eduapi.service;

import com.eduapi.dto.*;
import com.eduapi.entity.*;
import com.eduapi.exception.ResourceNotFoundException;
import com.eduapi.repository.StudentRepository;
import com.eduapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all students (paginated).
     */
    public Page<StudentDTO> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable).map(this::toDTO);
    }

    /**
     * Search students by name, email, or student code.
     */
    public Page<StudentDTO> searchStudents(String search, Pageable pageable) {
        if (search == null || search.isBlank()) {
            return getAllStudents(pageable);
        }
        return studentRepository.searchStudents(search, pageable).map(this::toDTO);
    }

    /**
     * Get a single student by ID.
     */
    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return toDTO(student);
    }

    /**
     * Get a student by their user ID (for self-access).
     */
    public StudentDTO getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "userId", userId));
        return toDTO(student);
    }

    /**
     * Create a new student (also creates the User record).
     */
    @Transactional
    public StudentDTO createStudent(StudentCreateDTO dto) {
        // Validate uniqueness
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered");
        }

        // Create User
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.STUDENT)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .phone(dto.getPhone())
                .profilePictureUrl(dto.getProfilePictureUrl())
                .build();
        user = userRepository.save(user);

        // Generate unique student code
        String studentCode = "STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create Student
        Student student = Student.builder()
                .user(user)
                .studentCode(studentCode)
                .dateOfBirth(dto.getDateOfBirth())
                .enrollmentDate(dto.getEnrollmentDate())
                .build();
        student = studentRepository.save(student);

        return toDTO(student);
    }

    /**
     * Update an existing student's profile info.
     */
    @Transactional
    public StudentDTO updateStudent(Long id, StudentUpdateDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));

        User user = student.getUser();

        // Check email uniqueness (if changed)
        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered");
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setProfilePictureUrl(dto.getProfilePictureUrl());
        userRepository.save(user);

        student.setDateOfBirth(dto.getDateOfBirth());
        student = studentRepository.save(student);

        return toDTO(student);
    }

    /**
     * Delete a student and their user account.
     */
    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        studentRepository.delete(student);
        userRepository.delete(student.getUser());
    }

    /**
     * Count total students.
     */
    public long countStudents() {
        return studentRepository.count();
    }

    // ─── Mapper ───

    private StudentDTO toDTO(Student student) {
        User user = student.getUser();
        return StudentDTO.builder()
                .id(student.getId())
                .userId(user.getId())
                .studentCode(student.getStudentCode())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profilePictureUrl(user.getProfilePictureUrl())
                .dateOfBirth(student.getDateOfBirth())
                .enrollmentDate(student.getEnrollmentDate())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
}

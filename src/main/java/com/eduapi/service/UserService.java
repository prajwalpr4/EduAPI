package com.eduapi.service;

import com.eduapi.dto.UserProfileDTO;
import com.eduapi.dto.UserProfileUpdateDTO;
import com.eduapi.entity.Student;
import com.eduapi.entity.User;
import com.eduapi.exception.ResourceNotFoundException;
import com.eduapi.repository.StudentRepository;
import com.eduapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public UserProfileDTO getProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return toProfileDTO(user);
    }

    @Transactional
    public UserProfileDTO updateProfile(String username, UserProfileUpdateDTO dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        // Check email uniqueness if changed
        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered");
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setProfilePictureUrl(dto.getProfilePictureUrl());
        user = userRepository.save(user);

        return toProfileDTO(user);
    }

    private UserProfileDTO toProfileDTO(User user) {
        UserProfileDTO.UserProfileDTOBuilder builder = UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .profilePictureUrl(user.getProfilePictureUrl())
                .createdAt(user.getCreatedAt());

        // Include student info if user is a student
        studentRepository.findByUserId(user.getId()).ifPresent(student -> {
            builder.studentId(student.getId());
            builder.studentCode(student.getStudentCode());
        });

        return builder.build();
    }
}

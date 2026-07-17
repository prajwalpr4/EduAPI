package com.eduapi.config;

import com.eduapi.entity.Role;
import com.eduapi.entity.Student;
import com.eduapi.entity.User;
import com.eduapi.repository.StudentRepository;
import com.eduapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Initializing default users in database...");

            // 1. Create Default Admin User
            User admin = User.builder()
                    .username("admin")
                    .email("admin@eduapi.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .firstName("System")
                    .lastName("Admin")
                    .phone("123-456-7890")
                    .build();
            userRepository.save(admin);
            log.info("Default Admin created: admin / admin123");

            // 2. Create Default Student User
            User studentUser = User.builder()
                    .username("student")
                    .email("student@eduapi.com")
                    .password(passwordEncoder.encode("student123"))
                    .role(Role.STUDENT)
                    .firstName("John")
                    .lastName("Doe")
                    .phone("987-654-3210")
                    .build();
            studentUser = userRepository.save(studentUser);

            // Create associated Student record
            Student student = Student.builder()
                    .user(studentUser)
                    .studentCode("STU-INITIAL01")
                    .dateOfBirth(LocalDate.of(2002, 5, 15))
                    .enrollmentDate(LocalDate.now())
                    .build();
            studentRepository.save(student);
            log.info("Default Student created: student / student123");
        } else {
            log.info("Database already contains users. Skipping default data initialization.");
        }
    }
}

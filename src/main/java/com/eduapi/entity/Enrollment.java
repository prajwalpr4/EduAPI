package com.eduapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enrollment entity — maps to the `enrollments` table.
 * Junction table linking Students to Courses, with grade and status.
 *
 * SQL columns → Entity fields mapping:
 *   id              → id
 *   student_id      → student (ManyToOne → Student.id)
 *   course_id       → course (ManyToOne → Course.id)
 *   enrollment_date → enrollmentDate
 *   grade           → grade
 *   status          → status
 *   created_at      → createdAt
 *   updated_at      → updatedAt
 */
@Entity
@Table(name = "enrollments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(length = 5)
    private String grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EnrollmentStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

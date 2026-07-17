package com.eduapi.controller;

import com.eduapi.service.CourseService;
import com.eduapi.service.EnrollmentService;
import com.eduapi.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Dashboard stats endpoint.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        Map<String, Long> stats = Map.of(
                "totalStudents", studentService.countStudents(),
                "totalCourses", courseService.countCourses(),
                "totalEnrollments", enrollmentService.countEnrollments()
        );
        return ResponseEntity.ok(stats);
    }
}

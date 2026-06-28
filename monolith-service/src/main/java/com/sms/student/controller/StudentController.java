package com.sms.student.controller;

import com.sms.common.dto.*;
import com.sms.student.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Students", description = "Student profile and dashboard APIs")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/api/students/dashboard")
    @Operation(summary = "Student dashboard with assigned subjects and latest notification")
    public ResponseEntity<ApiResponse<StudentDashboardResponse>> dashboard(
            @RequestHeader("X-Profile-Id") Long profileId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.dashboard(profileId)));
    }

    @GetMapping("/api/students/me")
    public ResponseEntity<ApiResponse<StudentResponse>> me(@RequestHeader("X-Profile-Id") Long profileId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.findById(profileId)));
    }

    @GetMapping("/api/students/subjects")
    @Operation(summary = "Subjects assigned to the current student")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> assignedSubjects(
            @RequestHeader("X-Profile-Id") Long profileId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getAssignedSubjects(profileId)));
    }

    @PostMapping("/api/students/internal")
    public ResponseEntity<ApiResponse<StudentResponse>> createInternal(@Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.create(request)));
    }

    @PutMapping("/api/students/internal/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> updateInternal(
            @PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.update(id, request)));
    }

    @DeleteMapping("/api/students/internal/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInternal(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    @GetMapping("/api/students/internal")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> findAllInternal() {
        return ResponseEntity.ok(ApiResponse.ok(studentService.findAll()));
    }

    @PatchMapping("/api/students/internal/{id}/activate")
    public ResponseEntity<ApiResponse<StudentResponse>> activateInternal(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.activate(id)));
    }
}

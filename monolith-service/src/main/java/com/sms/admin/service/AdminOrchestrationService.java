package com.sms.admin.service;

import com.sms.admin.exception.EmailDeliveryException;
import com.sms.auth.service.AuthService;
import com.sms.common.dto.*;
import com.sms.common.enums.UserRole;
import com.sms.common.security.EmailValidator;
import com.sms.common.security.InputSanitizer;
import com.sms.course.service.SubjectService;
import com.sms.notification.service.NotificationService;
import com.sms.student.service.StudentService;
import com.sms.teacher.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrchestrationService {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final AdminProfileService adminProfileService;
    private final SubjectService subjectService;
    private final NotificationService notificationService;
    private final AuthService authService;
    private final EmailService emailService;

    public StudentResponse createStudent(StudentRequest request) {
        EmailValidator.assertDeliverableRegistrationEmail(InputSanitizer.normalizeEmail(request.getEmail()));
        StudentResponse student = studentService.create(request);

        CreateInvitationRequest invitationRequest = new CreateInvitationRequest();
        invitationRequest.setEmail(student.getEmail());
        invitationRequest.setRole(UserRole.STUDENT);
        invitationRequest.setProfileId(student.getId());

        InvitationResponse invitation = authService.createInvitation(invitationRequest);
        sendRegistrationEmailOrExplain(student.getEmail(), student.getName(), UserRole.STUDENT, invitation);

        return student;
    }

    public StudentResponse updateStudent(Long id, StudentRequest request) {
        return studentService.update(id, request);
    }

    public void deleteStudent(Long id) {
        studentService.delete(id);
    }

    public List<StudentResponse> listStudents() {
        return studentService.findAll();
    }

    public TeacherResponse createTeacher(TeacherRequest request) {
        EmailValidator.assertDeliverableRegistrationEmail(InputSanitizer.normalizeEmail(request.getEmail()));
        TeacherResponse teacher = teacherService.create(request);

        CreateInvitationRequest invitationRequest = new CreateInvitationRequest();
        invitationRequest.setEmail(teacher.getEmail());
        invitationRequest.setRole(UserRole.TEACHER);
        invitationRequest.setProfileId(teacher.getId());

        InvitationResponse invitation = authService.createInvitation(invitationRequest);
        sendRegistrationEmailOrExplain(teacher.getEmail(), teacher.getName(), UserRole.TEACHER, invitation);

        return teacher;
    }

    private void sendRegistrationEmailOrExplain(
            String email, String name, UserRole role, InvitationResponse invitation) {
        try {
            emailService.sendRegistrationEmail(email, name, role, invitation);
        } catch (EmailDeliveryException ex) {
            throw new IllegalArgumentException(ex.getMessage()
                    + " User was created. Registration code: " + invitation.getRegistrationCode()
                    + " (share manually).");
        }
    }

    public TeacherResponse updateTeacher(Long id, TeacherRequest request) {
        return teacherService.update(id, request);
    }

    public void deleteTeacher(Long id) {
        teacherService.delete(id);
    }

    public List<TeacherResponse> listTeachers() {
        return teacherService.findAll();
    }

    public AdminUserResponse createAdmin(AdminUserRequest request) {
        return adminProfileService.create(request);
    }

    public AdminUserResponse updateAdmin(Long id, AdminUserRequest request) {
        return adminProfileService.update(id, request);
    }

    public void deleteAdmin(Long id) {
        adminProfileService.delete(id);
    }

    public List<AdminUserResponse> listAdmins() {
        return adminProfileService.findAll();
    }

    public List<SubjectResponse> listSubjects() {
        return subjectService.findAll();
    }

    public NotificationResponse createNotification(NotificationRequest request) {
        return notificationService.create(request);
    }

    public NotificationResponse updateNotification(Long id, NotificationRequest request) {
        return notificationService.update(id, request);
    }

    public void deleteNotification(Long id) {
        notificationService.delete(id);
    }

    public List<NotificationResponse> listNotifications() {
        return notificationService.findAll(null);
    }

    public void activateAdmin(Long id) {
        adminProfileService.activate(id);
    }

    public Map<String, Object> dashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("students", listStudents());
        dashboard.put("teachers", listTeachers());
        dashboard.put("admins", listAdmins());
        dashboard.put("subjects", listSubjects());
        dashboard.put("notifications", listNotifications());
        return dashboard;
    }
}

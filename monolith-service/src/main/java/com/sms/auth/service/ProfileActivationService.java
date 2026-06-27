package com.sms.auth.service;

import com.sms.admin.service.AdminProfileService;
import com.sms.common.enums.UserRole;
import com.sms.student.service.StudentService;
import com.sms.teacher.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileActivationService {

    private final StudentService studentService;
    private final TeacherService teacherService;
    private final AdminProfileService adminProfileService;

    public void activateProfile(UserRole role, Long profileId) {
        try {
            switch (role) {
                case STUDENT -> studentService.activate(profileId);
                case TEACHER -> teacherService.activate(profileId);
                case ADMIN -> adminProfileService.activate(profileId);
            }
        } catch (Exception ex) {
            log.warn("Failed to activate {} profile {}: {}", role, profileId, ex.getMessage());
        }
    }
}

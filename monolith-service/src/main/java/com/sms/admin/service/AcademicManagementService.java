package com.sms.admin.service;

import com.sms.auth.service.AuthService;
import com.sms.common.dto.*;
import com.sms.common.enums.UserRole;
import com.sms.common.security.EmailValidator;
import com.sms.common.security.InputSanitizer;
import com.sms.course.service.AcademicClassService;
import com.sms.course.service.AcademicStructureService;
import com.sms.course.service.DepartmentService;
import com.sms.course.service.SubjectService;
import com.sms.admin.exception.EmailDeliveryException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicManagementService {

    private final DepartmentService departmentService;
    private final AcademicClassService classService;
    private final SubjectService subjectService;
    private final AcademicStructureService structureService;

    public List<DepartmentResponse> listDepartments(Boolean activeOnly) {
        return Boolean.TRUE.equals(activeOnly)
                ? departmentService.findActive()
                : departmentService.findAll();
    }

    public DepartmentResponse createDepartment(DepartmentRequest request) {
        return departmentService.create(request);
    }

    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        return departmentService.update(id, request);
    }

    public void deleteDepartment(Long id) {
        departmentService.delete(id);
    }

    public List<AcademicClassResponse> listClasses(Long departmentId) {
        return departmentId != null
                ? classService.findByDepartmentId(departmentId)
                : classService.findAll();
    }

    public AcademicClassResponse createClass(AcademicClassRequest request) {
        return classService.create(request);
    }

    public AcademicClassResponse updateClass(Long id, AcademicClassRequest request) {
        return classService.update(id, request);
    }

    public void deleteClass(Long id) {
        classService.delete(id);
    }

    public List<SubjectResponse> listSubjects(Long classId) {
        return classId != null
                ? subjectService.findByClassId(classId)
                : subjectService.findAll();
    }

    public SubjectResponse createSubject(SubjectRequest request) {
        return subjectService.create(request);
    }

    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        return subjectService.update(id, request);
    }

    public void deleteSubject(Long id) {
        subjectService.delete(id);
    }

    public AcademicSelectionResponse validateSelection(AcademicSelectionRequest request) {
        return structureService.validateSelection(request);
    }
}

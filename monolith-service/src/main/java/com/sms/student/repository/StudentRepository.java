package com.sms.student.repository;

import com.sms.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    boolean existsByEmail(String email);

    List<Student> findByDepartmentIdAndClassIdOrderByNameAsc(Long departmentId, Long classId);
}

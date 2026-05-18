package com.example.courseselection.repository;

import com.example.courseselection.dto.ClassStatsDTO;
import com.example.courseselection.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    
    @Query(value = "CALL CalculateClassStats(:className)", nativeQuery = true)
    List<ClassStatsDTO> calculateClassStats(@Param("className") String className);

    Optional<Student> findByStudentNumber(String studentNumber);
}

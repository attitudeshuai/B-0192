package com.example.courseselection.repository;

import com.example.courseselection.dto.MakeupExamDTO;
import com.example.courseselection.entity.Selection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SelectionRepository extends JpaRepository<Selection, Long> {
    List<Selection> findByStudentId(Long studentId);

    @Query(value = "SELECT student_number AS studentNumber, class_name AS className, student_name AS studentName, course_name AS courseName, grade, major_name AS majorName FROM makeup_exam_view WHERE major_name = :majorName", nativeQuery = true)
    List<MakeupExamDTO> findMakeupExamsByMajor(@Param("majorName") String majorName);
}

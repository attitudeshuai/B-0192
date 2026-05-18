package com.example.courseselection.repository;

import com.example.courseselection.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseNumber(String courseNumber);
}

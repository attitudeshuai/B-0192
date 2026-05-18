package com.example.courseselection.service;

import com.example.courseselection.dto.ClassStatsDTO;
import com.example.courseselection.entity.Student;
import com.example.courseselection.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> saveAll(List<Student> students) {
        return studentRepository.saveAll(students);
    }

    public void updatePassword(Long id, String newPassword) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
        student.setPassword(newPassword);
        studentRepository.save(student);
    }

    public void deleteById(Long id) {
        studentRepository.deleteById(id);
    }

    public List<ClassStatsDTO> getClassStats(String className) {
        return studentRepository.calculateClassStats(className);
    }
}

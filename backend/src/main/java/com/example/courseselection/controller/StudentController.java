package com.example.courseselection.controller;

import com.example.courseselection.dto.ClassStatsDTO;
import com.example.courseselection.entity.Student;
import com.example.courseselection.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "*")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @GetMapping
    public List<Student> getAll() {
        return studentService.findAll();
    }

    @GetMapping("/{id}")
    public Student getById(@PathVariable Long id) {
        return studentService.findById(id).orElse(null);
    }

    @PostMapping
    public Student create(@RequestBody Student student) {
        return studentService.save(student);
    }

    @PostMapping("/batch")
    public List<Student> batchCreate(@RequestBody List<Student> students) {
        return studentService.saveAll(students);
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @RequestBody Student student) {
        student.setId(id);
        return studentService.save(student);
    }

    @PutMapping("/{id}/password")
    public void updatePassword(@PathVariable Long id, @RequestBody String newPassword) {
        studentService.updatePassword(id, newPassword);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        studentService.deleteById(id);
    }

    @GetMapping("/stats")
    public List<ClassStatsDTO> getClassStats(@RequestParam String className) {
        return studentService.getClassStats(className);
    }
}

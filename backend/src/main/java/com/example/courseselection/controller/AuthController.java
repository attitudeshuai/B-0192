package com.example.courseselection.controller;

import com.example.courseselection.dto.LoginRequest;
import com.example.courseselection.entity.Admin;
import com.example.courseselection.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private com.example.courseselection.repository.StudentRepository studentRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // 1. Try Admin Login
        var adminOpt = adminRepository.findByUsername(loginRequest.getUsername());
        if (adminOpt.isPresent()) {
            var admin = adminOpt.get();
            if (admin.getPassword().equals(loginRequest.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("token", "admin-token-" + admin.getId());
                response.put("username", admin.getUsername());
                response.put("role", "admin");
                return ResponseEntity.ok(response);
            }
        }

        // 2. Try Student Login
        var studentOpt = studentRepository.findByStudentNumber(loginRequest.getUsername());
        if (studentOpt.isPresent()) {
            var student = studentOpt.get();
            // Check password (assuming password field exists and is populated)
            String dbPassword = student.getPassword(); 
            // Default to 123456 if null (for compatibility with old data if not migrated)
            if (dbPassword == null) dbPassword = "123456"; 
            
            if (dbPassword.equals(loginRequest.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("token", "student-token-" + student.getId());
                response.put("username", student.getName());
                response.put("role", "student");
                response.put("userId", student.getId());
                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(401).body("用户名或密码错误");
    }
}

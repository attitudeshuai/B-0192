package com.example.courseselection.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String studentNumber;

    @Column(nullable = false)
    private String name;

    private String password;

    @Column(nullable = false)
    private String gender;

    private LocalDate birthDate;

    private String hometown;

    private String className;

    @ManyToOne
    @JoinColumn(name = "major_id")
    private Major major;
}

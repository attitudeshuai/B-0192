package com.example.courseselection.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String courseNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer credits;
}

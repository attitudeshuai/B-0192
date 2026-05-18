package com.example.courseselection;

import com.example.courseselection.entity.*;
import com.example.courseselection.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.util.Arrays;

@SpringBootApplication
public class CourseSelectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseSelectionApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(
            AdminRepository adminRepository,
            MajorRepository majorRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            SelectionRepository selectionRepository
    ) {
        return args -> {
            // Init Admin
            if (adminRepository.count() == 0) {
                Admin admin = new Admin();
                admin.setUsername("admin");
                admin.setPassword("123456");
                adminRepository.save(admin);
                System.out.println("Initialized default admin user: admin/123456");
            }

            // Init Majors
            if (majorRepository.count() == 0) {
                Major m1 = new Major();
                m1.setName("计算机科学与技术");
                m1.setDepartment("计算机学院");
                majorRepository.save(m1);

                Major m2 = new Major();
                m2.setName("软件工程");
                m2.setDepartment("软件学院");
                majorRepository.save(m2);
                System.out.println("Initialized majors");
            }

            // Init Courses
            if (courseRepository.count() == 0) {
                Course c1 = new Course();
                c1.setCourseNumber("CS101");
                c1.setName("数据库原理");
                c1.setCredits(3);
                courseRepository.save(c1);

                Course c2 = new Course();
                c2.setCourseNumber("CS102");
                c2.setName("操作系统");
                c2.setCredits(4);
                courseRepository.save(c2);
                System.out.println("Initialized courses");
            }

            // Init Students
            if (studentRepository.count() == 0) {
                Major majorCS = majorRepository.findAll().stream()
                        .filter(m -> m.getName().equals("计算机科学与技术")).findFirst().orElse(null);
                Major majorSE = majorRepository.findAll().stream()
                        .filter(m -> m.getName().equals("软件工程")).findFirst().orElse(null);

                Student s1 = new Student();
                s1.setStudentNumber("2023001");
                s1.setName("张三");
                s1.setGender("男");
                s1.setBirthDate(LocalDate.of(2000, 1, 1));
                s1.setHometown("北京");
                s1.setClassName("计科1班");
                s1.setMajor(majorCS);
                s1.setPassword("123456");
                studentRepository.save(s1);

                Student s2 = new Student();
                s2.setStudentNumber("2023002");
                s2.setName("李四");
                s2.setGender("女");
                s2.setBirthDate(LocalDate.of(2000, 2, 2));
                s2.setHometown("上海");
                s2.setClassName("软件1班");
                s2.setMajor(majorSE);
                s2.setPassword("123456");
                studentRepository.save(s2);
                System.out.println("Initialized students");
            } else {
                // Fix existing students with null passwords
                studentRepository.findAll().forEach(s -> {
                    if (s.getPassword() == null) {
                        s.setPassword("123456");
                        studentRepository.save(s);
                        System.out.println("Updated password for student: " + s.getName());
                    }
                });
            }

            // Init Selections
            if (selectionRepository.count() == 0 && studentRepository.count() > 0 && courseRepository.count() > 0) {
                Student s1 = studentRepository.findByStudentNumber("2023001").orElse(null);
                Student s2 = studentRepository.findByStudentNumber("2023002").orElse(null);
                Course c1 = courseRepository.findByCourseNumber("CS101").orElse(null); // DB
                Course c2 = courseRepository.findByCourseNumber("CS102").orElse(null); // OS

                if (s1 != null && c1 != null) {
                    Selection sel1 = new Selection();
                    sel1.setStudent(s1);
                    sel1.setCourse(c1);
                    // sel1.setGrade(null); // Not graded yet
                    selectionRepository.save(sel1);
                }

                if (s2 != null && c2 != null) {
                    Selection sel2 = new Selection();
                    sel2.setStudent(s2);
                    sel2.setCourse(c2);
                    selectionRepository.save(sel2);
                }
                System.out.println("Initialized selections");
            }
        };
    }

}

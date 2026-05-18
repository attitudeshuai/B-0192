package com.example.courseselection.controller;

import com.example.courseselection.entity.Major;
import com.example.courseselection.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/majors")
@CrossOrigin(origins = "*")
public class MajorController {
    @Autowired
    private MajorService majorService;

    @GetMapping
    public List<Major> getAll() {
        return majorService.findAll();
    }

    @GetMapping("/{id}")
    public Major getById(@PathVariable Long id) {
        return majorService.findById(id).orElse(null);
    }

    @PostMapping
    public Major create(@RequestBody Major major) {
        return majorService.save(major);
    }

    @PutMapping("/{id}")
    public Major update(@PathVariable Long id, @RequestBody Major major) {
        major.setId(id);
        return majorService.save(major);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        majorService.deleteById(id);
    }
}

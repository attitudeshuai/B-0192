package com.example.courseselection.controller;

import com.example.courseselection.dto.MakeupExamDTO;
import com.example.courseselection.entity.Selection;
import com.example.courseselection.service.SelectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/selections")
@CrossOrigin(origins = "*")
public class SelectionController {
    @Autowired
    private SelectionService selectionService;

    @GetMapping
    public List<Selection> getAll() {
        return selectionService.findAll();
    }

    @GetMapping("/student/{studentId}")
    public List<Selection> getByStudentId(@PathVariable Long studentId) {
        return selectionService.findByStudentId(studentId);
    }

    @PostMapping
    public Selection create(@RequestBody Selection selection) {
        return selectionService.save(selection);
    }

    @PutMapping("/{id}")
    public Selection update(@PathVariable Long id, @RequestBody Selection selection) {
        selection.setId(id);
        return selectionService.save(selection);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        selectionService.deleteById(id);
    }

    @GetMapping("/makeup")
    public List<MakeupExamDTO> getMakeupExams(@RequestParam String majorName) {
        return selectionService.getMakeupExams(majorName);
    }
}

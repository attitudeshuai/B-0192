package com.example.courseselection.service;

import com.example.courseselection.dto.MakeupExamDTO;
import com.example.courseselection.entity.Selection;
import com.example.courseselection.repository.SelectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SelectionService {
    @Autowired
    private SelectionRepository selectionRepository;

    public List<Selection> findAll() {
        return selectionRepository.findAll();
    }

    public List<Selection> findByStudentId(Long studentId) {
        return selectionRepository.findByStudentId(studentId);
    }

    public Optional<Selection> findById(Long id) {
        return selectionRepository.findById(id);
    }

    public Selection save(Selection selection) {
        return selectionRepository.save(selection);
    }

    public void deleteById(Long id) {
        selectionRepository.deleteById(id);
    }

    public List<MakeupExamDTO> getMakeupExams(String majorName) {
        return selectionRepository.findMakeupExamsByMajor(majorName);
    }
}

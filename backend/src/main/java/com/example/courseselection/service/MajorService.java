package com.example.courseselection.service;

import com.example.courseselection.entity.Major;
import com.example.courseselection.repository.MajorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MajorService {
    @Autowired
    private MajorRepository majorRepository;

    public List<Major> findAll() {
        return majorRepository.findAll();
    }

    public Optional<Major> findById(Long id) {
        return majorRepository.findById(id);
    }

    public Major save(Major major) {
        return majorRepository.save(major);
    }

    public void deleteById(Long id) {
        majorRepository.deleteById(id);
    }
}

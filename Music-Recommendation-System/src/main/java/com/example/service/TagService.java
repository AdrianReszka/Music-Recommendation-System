package com.example.service;

import com.example.model.Tag;
import com.example.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> getAll() {
        return tagRepository.findAll();
    }

    public Tag create(Tag tag) {
        return tagRepository.save(tag);
    }

    public void delete(Long id) {
        tagRepository.deleteById(id);
    }
}


package com.example.controller;

import com.example.model.Tag;
import com.example.repository.TagRepository;
import com.example.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musicapp/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<Tag> getAllTags() {
        return tagService.getAll();
    }

    @PostMapping
    public Tag createTag(@RequestBody Tag tag) {
        return tagService.create(tag);
    }

    @DeleteMapping("/{id}")
    public void deleteTag(@PathVariable Long id) {
        tagService.delete(id);
    }
}

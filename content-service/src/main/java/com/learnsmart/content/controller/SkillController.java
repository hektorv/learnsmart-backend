package com.learnsmart.content.controller;

import com.learnsmart.content.model.Skill;
import com.learnsmart.content.model.Domain;
import com.learnsmart.content.dto.ContentDtos;
import com.learnsmart.content.service.SkillService;
import com.learnsmart.content.service.DomainService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final DomainService domainService;

    @GetMapping
    public List<Skill> getSkills(@RequestParam(required = false) UUID domainId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return skillService.findAll(domainId, null, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Skill> getSkill(@PathVariable UUID id) {
        return skillService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Skill> createSkill(@RequestBody ContentDtos.SkillInput input) {
        Domain domain = domainService.findById(input.getDomainId())
                .orElseThrow(() -> new com.learnsmart.content.exception.DomainNotFoundException(input.getDomainId()));

        Skill s = new Skill();
        s.setDomain(domain);
        s.setCode(input.getCode());
        s.setName(input.getName());
        s.setDescription(input.getDescription());
        s.setLevel(input.getLevel());
        s.setTags(input.getTags());

        return new ResponseEntity<>(skillService.create(s), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Skill> updateSkill(@PathVariable UUID id, @RequestBody ContentDtos.SkillInput input) {
        Skill s = new Skill();
        s.setName(input.getName());
        s.setDescription(input.getDescription());
        s.setLevel(input.getLevel());
        s.setTags(input.getTags());

        return skillService.update(id, s)
                .map(updated -> ResponseEntity.ok(updated))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // US-081: Explicit Prerequisite Management
    @GetMapping("/{id}/prerequisites")
    public ResponseEntity<List<ContentDtos.SkillDto>> getPrerequisites(@PathVariable UUID id) {
        List<Skill> prereqs = skillService.getPrerequisites(id);
        List<ContentDtos.SkillDto> dtos = prereqs.stream().map(skill -> {
            ContentDtos.SkillDto dto = new ContentDtos.SkillDto();
            dto.setId(skill.getId());
            dto.setName(skill.getName());
            dto.setDescription(skill.getDescription());
            dto.setLevel(skill.getLevel());
            return dto;
        }).toList();
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}/prerequisites")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updatePrerequisites(@PathVariable UUID id, @RequestBody List<UUID> prerequisiteIds) {
        skillService.updatePrerequisites(id, prerequisiteIds);
        return ResponseEntity.noContent().build();
    }

    // US-10-06: AI Skill Discovery (Taxonomy)
    @PostMapping("/domains/{domainId}/generate")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Skill>> generateSkills(@PathVariable UUID domainId,
            @RequestBody ContentDtos.GenerateSkillsInput input) {
        List<Skill> skills = skillService.generateSkills(domainId, input.getTopic());
        return new ResponseEntity<>(skills, HttpStatus.CREATED);
    }

    // US-10-07: AI Prerequisite Linking (Graph)
    @PostMapping("/domains/{domainId}/link")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> linkSkills(@PathVariable UUID domainId) {
        skillService.linkSkills(domainId);
        return ResponseEntity.noContent().build();
    }
}

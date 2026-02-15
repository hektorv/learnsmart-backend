package com.learnsmart.content.service;

import com.learnsmart.content.model.Skill;
import com.learnsmart.content.model.Domain;
import com.learnsmart.content.repository.SkillRepository;
import com.learnsmart.content.client.AiServiceClient;
import com.learnsmart.content.dto.AiDtos;
import com.learnsmart.content.exception.DomainNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final DomainService domainService;
    private final AiServiceClient aiServiceClient;

    @Override
    public List<Skill> findAll(UUID domainId, String search, Integer page, Integer size) {
        if (domainId != null) {
            return skillRepository.findByDomainId(domainId);
        }
        return skillRepository.findAll();
    }

    @Override
    public Optional<Skill> findById(UUID id) {
        return skillRepository.findById(id);
    }

    @Override
    @Transactional
    public Skill create(Skill skill) {
        return skillRepository.save(skill);
    }

    @Override
    @Transactional
    public Optional<Skill> update(UUID id, Skill skill) {
        return skillRepository.findById(id).map(existing -> {
            existing.setName(skill.getName());
            existing.setDescription(skill.getDescription());
            existing.setLevel(skill.getLevel());
            existing.setTags(skill.getTags());
            return skillRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        skillRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Skill> getPrerequisites(UUID id) {
        return skillRepository.findById(id)
                .map(s -> List.copyOf(s.getPrerequisites()))
                .orElse(Collections.emptyList());
    }

    @Override
    @Transactional
    public void updatePrerequisites(UUID id, List<UUID> prerequisiteIds) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found: " + id));

        // 1. Validate Self-Reference
        if (prerequisiteIds.contains(id)) {
            throw new IllegalArgumentException("Skill cannot depend on itself");
        }

        // 2. Fetch Prerequisite Entities
        List<Skill> newPrereqs = skillRepository.findAllById(prerequisiteIds);
        if (newPrereqs.size() != prerequisiteIds.size()) {
            throw new IllegalArgumentException("One or more prerequisite IDs not found");
        }

        // 3. Cycle Detection
        for (Skill prereq : newPrereqs) {
            checkCycle(prereq, id, new HashSet<>());
        }

        skill.setPrerequisites(new HashSet<>(newPrereqs));
        skillRepository.save(skill);
    }

    @Override
    @Transactional
    public List<Skill> generateSkills(UUID domainId, String topic) {
        // US-10-06: AI Skill Discovery (Taxonomy)

        // 1. Validate domain exists
        Domain domain = domainService.findById(domainId)
                .orElseThrow(() -> new DomainNotFoundException("Domain not found: " + domainId));

        // 2. Call AI service to generate skill taxonomy
        AiDtos.GenerateSkillsRequest aiRequest = AiDtos.GenerateSkillsRequest.builder()
                .topic(topic)
                .domainId(domain.getName()) // Pass domain name, not UUID
                .build();

        AiDtos.GenerateSkillsResponse aiResponse = aiServiceClient.generateSkills(aiRequest);

        // 3. Convert AI drafts to Skill entities and persist
        List<Skill> createdSkills = aiResponse.getSkills().stream()
                .map(draft -> {
                    Skill skill = new Skill();
                    skill.setDomain(domain);
                    skill.setCode(draft.getCode());
                    skill.setName(draft.getName());
                    skill.setDescription(draft.getDescription());
                    skill.setLevel(draft.getLevel());
                    skill.setTags(draft.getTags());
                    skill.setPrerequisites(new java.util.HashSet<>());
                    return skillRepository.save(skill);
                })
                .collect(java.util.stream.Collectors.toList());

        return createdSkills;
    }

    @Override
    @Transactional
    public void linkSkills(UUID domainId) {
        // US-10-07: AI Prerequisite Linking (Graph)

        // 1. Fetch all skills for the domain
        List<Skill> domainSkills = skillRepository.findByDomainId(domainId);

        if (domainSkills.isEmpty()) {
            throw new IllegalArgumentException("No skills found for domain: " + domainId);
        }

        // 2. Convert to AI drafts
        List<AiDtos.SkillDraft> skillDrafts = domainSkills.stream()
                .map(skill -> AiDtos.SkillDraft.builder()
                        .code(skill.getCode())
                        .name(skill.getName())
                        .description(skill.getDescription())
                        .level(skill.getLevel())
                        .tags(skill.getTags())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        // 3. Call AI service to generate prerequisite graph
        AiDtos.GeneratePrerequisitesRequest aiRequest = AiDtos.GeneratePrerequisitesRequest.builder()
                .skills(skillDrafts)
                .build();

        AiDtos.GeneratePrerequisitesResponse aiResponse = aiServiceClient.generatePrerequisites(aiRequest);

        // 4. Create a map of code -> Skill for quick lookup
        Map<String, Skill> skillByCode = domainSkills.stream()
                .collect(java.util.stream.Collectors.toMap(Skill::getCode, s -> s));

        // 5. Update prerequisites for each skill
        for (AiDtos.PrerequisiteLink link : aiResponse.getPrerequisites()) {
            Skill skill = skillByCode.get(link.getSkillCode());
            if (skill != null && link.getPrerequisiteCodes() != null) {
                Set<Skill> prerequisites = link.getPrerequisiteCodes().stream()
                        .map(skillByCode::get)
                        .filter(java.util.Objects::nonNull)
                        .collect(java.util.stream.Collectors.toSet());

                skill.setPrerequisites(prerequisites);
                skillRepository.save(skill);
            }
        }
    }

    private void checkCycle(Skill current, UUID targetId, Set<UUID> visited) {
        if (current.getId().equals(targetId)) {
            throw new IllegalArgumentException("Circular dependency detected involving skill: " + current.getName());
        }
        if (!visited.add(current.getId())) {
            return;
        }
        for (Skill dependency : current.getPrerequisites()) {
            // Ensure dependencies are loaded (transactional session needed)
            checkCycle(dependency, targetId, visited);
        }
    }
}

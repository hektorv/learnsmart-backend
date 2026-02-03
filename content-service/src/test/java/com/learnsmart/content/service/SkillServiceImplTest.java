package com.learnsmart.content.service;

import com.learnsmart.content.model.Skill;
import com.learnsmart.content.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Test
    void testFindAll_WithDomainId() {
        UUID domainId = UUID.randomUUID();
        when(skillRepository.findByDomainId(domainId)).thenReturn(Collections.emptyList());

        List<Skill> result = skillService.findAll(domainId, null, null, 0, 10);
        assertNotNull(result);
        verify(skillRepository).findByDomainId(domainId);
    }

    @Test
    void testFindAll_WithCode() {
        String code = "MATH";
        when(skillRepository.findByCodeContaining(code)).thenReturn(Collections.emptyList());

        List<Skill> result = skillService.findAll(null, code, null, 0, 10);
        assertNotNull(result);
        verify(skillRepository).findByCodeContaining(code);
    }

    @Test
    void testFindAll_NoFilters() {
        when(skillRepository.findAll()).thenReturn(Collections.emptyList());
        List<Skill> result = skillService.findAll(null, null, null, 0, 10);
        assertNotNull(result);
        verify(skillRepository).findAll();
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        Skill skill = new Skill();
        skill.setId(id);
        when(skillRepository.findById(id)).thenReturn(Optional.of(skill));

        Optional<Skill> result = skillService.findById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void testCreate() {
        Skill skill = new Skill();
        when(skillRepository.save(skill)).thenReturn(skill);

        Skill result = skillService.create(skill);
        assertNotNull(result);
        verify(skillRepository).save(skill);
    }

    @Test
    void testUpdate_Found() {
        UUID id = UUID.randomUUID();
        Skill existing = new Skill();
        existing.setId(id);
        Skill update = new Skill();
        update.setName("New Name");

        when(skillRepository.findById(id)).thenReturn(Optional.of(existing));
        when(skillRepository.save(existing)).thenReturn(existing);

        Optional<Skill> result = skillService.update(id, update);
        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
        verify(skillRepository).save(existing);
    }

    @Test
    void testUpdate_NotFound() {
        UUID id = UUID.randomUUID();
        when(skillRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Skill> result = skillService.update(id, new Skill());
        assertFalse(result.isPresent());
    }

    @Test
    void testDelete() {
        UUID id = UUID.randomUUID();
        doNothing().when(skillRepository).deleteById(id);
        skillService.delete(id);
        verify(skillRepository).deleteById(id);
    }

    @Test
    void testGetPrerequisites() {
        UUID id = UUID.randomUUID();
        Skill skill = new Skill();
        Skill prereq = new Skill();
        skill.setPrerequisites(Set.of(prereq));

        when(skillRepository.findById(id)).thenReturn(Optional.of(skill));

        List<Skill> result = skillService.getPrerequisites(id);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdatePrerequisites() {
        UUID id = UUID.randomUUID();
        UUID prereqId = UUID.randomUUID();
        Skill skill = new Skill();
        Skill prereq = new Skill();
        prereq.setId(prereqId);

        when(skillRepository.findById(id)).thenReturn(Optional.of(skill));
        when(skillRepository.findAllById(List.of(prereqId))).thenReturn(List.of(prereq));
        when(skillRepository.save(skill)).thenReturn(skill);

        skillService.updatePrerequisites(id, List.of(prereqId));

        assertEquals(1, skill.getPrerequisites().size());
        assertTrue(skill.getPrerequisites().contains(prereq));
        verify(skillRepository).save(skill);
    }

    @Test
    void testUpdatePrerequisites_SkillNotFound() {
        UUID id = UUID.randomUUID();
        when(skillRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> skillService.updatePrerequisites(id, List.of()));
    }
}

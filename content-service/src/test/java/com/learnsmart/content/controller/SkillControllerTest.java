package com.learnsmart.content.controller;

import com.learnsmart.content.dto.ContentDtos;
import com.learnsmart.content.model.Domain;
import com.learnsmart.content.model.Skill;
import com.learnsmart.content.service.DomainService;
import com.learnsmart.content.service.SkillService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillControllerTest {

    @Mock
    private SkillService skillService;

    @Mock
    private DomainService domainService;

    @InjectMocks
    private SkillController controller;

    @Test
    void testGetSkills() {
        UUID domainId = UUID.randomUUID();

        when(skillService.findAll(eq(domainId), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        List<Skill> result = controller.getSkills(domainId, 0, 10);
        assertTrue(result.isEmpty());
        verify(skillService).findAll(eq(domainId), any(), eq(0), eq(10));
    }

    @Test
    void testCreateSkill_Success() {
        UUID domainId = UUID.randomUUID();
        ContentDtos.SkillInput input = new ContentDtos.SkillInput();
        input.setDomainId(domainId);
        input.setCode("ALG-001");

        Domain domain = new Domain();
        domain.setId(domainId);

        when(domainService.findById(domainId)).thenReturn(Optional.of(domain));
        when(skillService.create(any(Skill.class))).thenAnswer(i -> {
            Skill s = i.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        ResponseEntity<Skill> response = controller.createSkill(input);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void testCreateSkill_DomainNotFound() {
        UUID domainId = UUID.randomUUID();
        ContentDtos.SkillInput input = new ContentDtos.SkillInput();
        input.setDomainId(domainId);

        when(domainService.findById(domainId)).thenReturn(Optional.empty());

        assertThrows(com.learnsmart.content.exception.DomainNotFoundException.class,
                () -> controller.createSkill(input));
    }
}

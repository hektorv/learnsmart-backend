package com.learnsmart.content.service;

import com.learnsmart.content.client.AiServiceClient;
import com.learnsmart.content.dto.AiDtos;
import com.learnsmart.content.model.ContentItem;
import com.learnsmart.content.model.ContentItemSkill;
import com.learnsmart.content.model.Domain;
import com.learnsmart.content.model.Skill;
import com.learnsmart.content.repository.ContentItemRepository;
import com.learnsmart.content.repository.ContentItemSkillRepository;
import com.learnsmart.content.repository.DomainRepository;
import com.learnsmart.content.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentItemServiceImplTest {

    @Mock
    private ContentItemRepository contentItemRepository;
    @Mock
    private SkillRepository skillRepository;
    @Mock
    private ContentItemSkillRepository contentItemSkillRepository;
    @Mock
    private AiServiceClient aiServiceClient;
    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private ContentItemServiceImpl contentItemService;

    @Test
    void testFindAll_WithDomainId() {
        UUID domainId = UUID.randomUUID();
        when(contentItemRepository.findByDomainId(domainId)).thenReturn(Collections.emptyList());

        List<ContentItem> result = contentItemService.findAll(domainId, null, null, null, 0, 10);
        assertNotNull(result);
        verify(contentItemRepository).findByDomainId(domainId);
    }

    @Test
    void testFindAll_WithType() {
        String type = "lesson";
        when(contentItemRepository.findByType(type)).thenReturn(Collections.emptyList());

        List<ContentItem> result = contentItemService.findAll(null, null, type, null, 0, 10);
        assertNotNull(result);
        verify(contentItemRepository).findByType(type);
    }

    @Test
    void testFindAll_NoFilters() {
        when(contentItemRepository.findAll()).thenReturn(Collections.emptyList());

        List<ContentItem> result = contentItemService.findAll(null, null, null, null, 0, 10);
        assertNotNull(result);
        verify(contentItemRepository).findAll();
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        ContentItem item = new ContentItem();
        item.setId(id);
        when(contentItemRepository.findById(id)).thenReturn(Optional.of(item));

        Optional<ContentItem> result = contentItemService.findById(id);
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void testCreate() {
        ContentItem item = new ContentItem();
        when(contentItemRepository.save(item)).thenReturn(item);

        ContentItem result = contentItemService.create(item);
        assertNotNull(result);
        verify(contentItemRepository).save(item);
    }

    @Test
    void testUpdate_Found() {
        UUID id = UUID.randomUUID();
        ContentItem existing = new ContentItem();
        existing.setId(id);

        ContentItem updateRequest = new ContentItem();
        updateRequest.setTitle("New Title");
        updateRequest.setDescription("New Desc");
        updateRequest.setEstimatedMinutes(10);
        updateRequest.setDifficulty(BigDecimal.valueOf(0.8));
        updateRequest.setMetadata(Collections.emptyMap());
        updateRequest.setActive(true);

        when(contentItemRepository.findById(id)).thenReturn(Optional.of(existing));
        when(contentItemRepository.save(existing)).thenReturn(existing);

        Optional<ContentItem> result = contentItemService.update(id, updateRequest);

        assertTrue(result.isPresent());
        assertEquals("New Title", result.get().getTitle());
        verify(contentItemRepository).save(existing);
    }

    @Test
    void testUpdate_NotFound() {
        UUID id = UUID.randomUUID();
        ContentItem updateRequest = new ContentItem();
        when(contentItemRepository.findById(id)).thenReturn(Optional.empty());

        Optional<ContentItem> result = contentItemService.update(id, updateRequest);
        assertFalse(result.isPresent());
        verify(contentItemRepository, never()).save(any());
    }

    @Test
    void testDelete() {
        UUID id = UUID.randomUUID();
        doNothing().when(contentItemRepository).deleteById(id);
        contentItemService.delete(id);
        verify(contentItemRepository).deleteById(id);
    }

    @Test
    void testUpdateSkillAssociations() {
        UUID contentItemId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();
        ContentItem item = new ContentItem();
        Skill skill = new Skill();

        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.of(item));
        when(skillRepository.findById(skillId)).thenReturn(Optional.of(skill));

        contentItemService.updateSkillAssociations(contentItemId, List.of(skillId), List.of(0.5));

        verify(contentItemSkillRepository).save(any(ContentItemSkill.class));
    }

    @Test
    void testUpdateSkillAssociations_ContentItemNotFound() {
        UUID contentItemId = UUID.randomUUID();
        when(contentItemRepository.findById(contentItemId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> contentItemService.updateSkillAssociations(contentItemId, List.of(), List.of()));
    }

    @Test
    void testGenerateAndSave() {
        UUID domainId = UUID.randomUUID();
        Domain domain = new Domain();
        domain.setName("Math");

        when(domainRepository.findById(domainId)).thenReturn(Optional.of(domain));

        AiDtos.ContentLessonDraft draft = AiDtos.ContentLessonDraft.builder()
                .title("Lesson 1")
                .description("Desc")
                .estimatedMinutes(15)
                .difficulty(0.3)
                .type("video")
                .body("Content")
                .build();

        AiDtos.GenerateLessonsResponse response = AiDtos.GenerateLessonsResponse.builder()
                .lessons(List.of(draft))
                .build();

        when(aiServiceClient.generateLessons(any(AiDtos.GenerateLessonsRequest.class))).thenReturn(response);
        when(contentItemRepository.save(any(ContentItem.class))).thenAnswer(i -> i.getArgument(0));

        List<ContentItem> result = contentItemService.generateAndSave(domainId, 1, "Topic");

        assertFalse(result.isEmpty());
        assertEquals("Lesson 1", result.get(0).getTitle());
        verify(aiServiceClient).generateLessons(any());
        verify(contentItemRepository).save(any());
    }
}

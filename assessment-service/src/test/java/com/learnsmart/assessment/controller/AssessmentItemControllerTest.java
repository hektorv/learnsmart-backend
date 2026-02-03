package com.learnsmart.assessment.controller;

import com.learnsmart.assessment.dto.AssessmentDtos;
import com.learnsmart.assessment.model.AssessmentItem;
import com.learnsmart.assessment.service.AssessmentItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentItemControllerTest {

    @Mock
    private AssessmentItemService assessmentItemService;

    @InjectMocks
    private AssessmentItemController controller;

    @Test
    void testCreate() {
        AssessmentDtos.AssessmentItemInput input = new AssessmentDtos.AssessmentItemInput();
        input.setDomainId(UUID.randomUUID());
        input.setStem("Question");
        input.setDifficulty(0.5);

        when(assessmentItemService.create(any(AssessmentItem.class))).thenAnswer(i -> {
            AssessmentItem res = i.getArgument(0);
            res.setId(UUID.randomUUID());
            return res;
        });

        ResponseEntity<AssessmentItem> response = controller.create(input);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void testFindAll() {
        when(assessmentItemService.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<List<AssessmentItem>> response = controller.findAll();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testFindById_Found() {
        UUID id = UUID.randomUUID();
        AssessmentItem item = new AssessmentItem();
        item.setId(id);
        when(assessmentItemService.findById(id)).thenReturn(Optional.of(item));

        ResponseEntity<AssessmentItem> response = controller.findById(id);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(id, response.getBody().getId());
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();
        when(assessmentItemService.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<AssessmentItem> response = controller.findById(id);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}

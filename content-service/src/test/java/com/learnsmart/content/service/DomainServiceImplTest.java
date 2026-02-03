package com.learnsmart.content.service;

import com.learnsmart.content.model.Domain;
import com.learnsmart.content.repository.DomainRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DomainServiceImplTest {

    @Mock
    private DomainRepository domainRepository;

    @InjectMocks
    private DomainServiceImpl domainService;

    @Test
    void testFindAll_WithCode() {
        String code = "MATH";
        Domain domain = new Domain();
        domain.setCode(code);
        when(domainRepository.findByCode(code)).thenReturn(Optional.of(domain));

        List<Domain> result = domainService.findAll(code, 0, 10);
        assertEquals(1, result.size());
        assertEquals(code, result.get(0).getCode());
    }

    @Test
    void testFindAll_WithCode_NotFound() {
        String code = "MATH";
        when(domainRepository.findByCode(code)).thenReturn(Optional.empty());

        List<Domain> result = domainService.findAll(code, 0, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAll_NoFilter() {
        when(domainRepository.findAll()).thenReturn(Collections.emptyList());
        List<Domain> result = domainService.findAll(null, 0, 10);
        assertNotNull(result);
        verify(domainRepository).findAll();
    }

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        Domain domain = new Domain();
        domain.setId(id);
        when(domainRepository.findById(id)).thenReturn(Optional.of(domain));

        Optional<Domain> result = domainService.findById(id);
        assertTrue(result.isPresent());
    }

    @Test
    void testCreate_Success() {
        Domain domain = new Domain();
        domain.setCode("MATH");

        when(domainRepository.findByCode("MATH")).thenReturn(Optional.empty());
        when(domainRepository.save(domain)).thenReturn(domain);

        Domain result = domainService.create(domain);
        assertNotNull(result);
    }

    @Test
    void testCreate_DuplicateCode() {
        Domain domain = new Domain();
        domain.setCode("MATH");

        when(domainRepository.findByCode("MATH")).thenReturn(Optional.of(new Domain()));

        assertThrows(IllegalArgumentException.class, () -> domainService.create(domain));
        verify(domainRepository, never()).save(any());
    }

    @Test
    void testUpdate_Found() {
        UUID id = UUID.randomUUID();
        Domain existing = new Domain();
        existing.setId(id);
        Domain update = new Domain();
        update.setName("New Name");

        when(domainRepository.findById(id)).thenReturn(Optional.of(existing));
        when(domainRepository.save(existing)).thenReturn(existing);

        Optional<Domain> result = domainService.update(id, update);
        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
    }

    @Test
    void testUpdate_NotFound() {
        UUID id = UUID.randomUUID();
        when(domainRepository.findById(id)).thenReturn(Optional.empty());
        Optional<Domain> result = domainService.update(id, new Domain());
        assertFalse(result.isPresent());
    }

    @Test
    void testDelete() {
        UUID id = UUID.randomUUID();
        doNothing().when(domainRepository).deleteById(id);
        domainService.delete(id);
        verify(domainRepository).deleteById(id);
    }
}

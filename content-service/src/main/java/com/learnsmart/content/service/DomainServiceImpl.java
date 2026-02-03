package com.learnsmart.content.service;

import com.learnsmart.content.model.Domain;
import com.learnsmart.content.repository.DomainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DomainServiceImpl implements DomainService {

    private final DomainRepository domainRepository;

    @Override
    public List<Domain> findAll(String code, String status, Integer page, Integer size) {
        if (code != null) {
            Optional<Domain> d = domainRepository.findByCode(code);
            return d.map(List::of).orElse(List.of());
        }
        if (status != null) {
            return domainRepository.findByStatus(status);
        }
        // US-088: Default filter? Or return all?
        // If status is null, usually return all (Admin behavior) or default to
        // published (User behavior).
        // Let's assume the Controller handles the default, or we default here.
        // Given "Update findAll to filter by status by default", let's default to
        // 'published' if null?
        // But that breaks "List all for admin".
        // Let's modify Controller to pass "published" by default if user is
        // unprivileged, or just support the param.
        return domainRepository.findAll();
    }

    @Override
    public Optional<Domain> findById(UUID id) {
        return domainRepository.findById(id);
    }

    @Override
    @Transactional
    public Domain create(Domain domain) {
        if (domainRepository.findByCode(domain.getCode()).isPresent()) {
            throw new IllegalArgumentException("Domain code already exists: " + domain.getCode());
        }
        return domainRepository.save(domain);
    }

    @Override
    @Transactional
    public Optional<Domain> update(UUID id, Domain domain) {
        return domainRepository.findById(id).map(existing -> {
            existing.setName(domain.getName());
            existing.setDescription(domain.getDescription());
            // Code typically not updateable or requires check
            return domainRepository.save(existing);
        });
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        domainRepository.deleteById(id);
    }
}

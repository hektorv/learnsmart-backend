package com.learnsmart.planning.repository;

import com.learnsmart.planning.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    List<Certificate> findByUserId(UUID userId);

    boolean existsByPlanId(UUID planId);
}

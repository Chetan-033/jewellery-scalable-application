package com.jewellery.erp.organization.repository;

import com.jewellery.erp.organization.domain.DocumentSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface DocumentSequenceRepository extends JpaRepository<DocumentSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<DocumentSequence> findByDocType(String docType);
}

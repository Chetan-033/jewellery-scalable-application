package com.jewellery.erp.organization.application;

import com.jewellery.erp.organization.domain.DocumentSequence;
import com.jewellery.erp.organization.repository.DocumentSequenceRepository;
import com.jewellery.erp.shared.domain.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentNumberService {

    private final DocumentSequenceRepository repository;

    public DocumentNumberService(DocumentSequenceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String next(String docType) {
        DocumentSequence seq = repository.findByDocType(docType)
                .orElseThrow(() -> new BusinessException("SEQ_MISSING", "Document sequence not configured: " + docType));
        long value = seq.getNextValue();
        seq.setNextValue(value + 1);
        repository.save(seq);
        return seq.getPrefix() + String.format("%05d", value);
    }
}

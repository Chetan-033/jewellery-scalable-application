package com.jewellery.erp.organization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_sequence")
public class DocumentSequence {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "doc_type", nullable = false, length = 32)
    private String docType;

    @Column(nullable = false, length = 32)
    private String prefix;

    @Column(name = "next_value", nullable = false)
    private long nextValue = 1;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public long getNextValue() { return nextValue; }
    public void setNextValue(long nextValue) { this.nextValue = nextValue; }
}

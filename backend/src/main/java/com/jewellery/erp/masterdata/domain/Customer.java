package com.jewellery.erp.masterdata.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    private String phone;
    private String email;
    private String gstin;
    private String city;

    @Column(length = 500)
    private String address;

    @Column(name = "party_type", nullable = false, length = 32)
    private String partyType = "CUSTOMER";

    @Column(nullable = false, length = 32)
    private String status = "ACTIVE";

    @Column(length = 16)
    private String title;

    @Column(length = 8)
    private String gender;

    @Column(name = "spouse_name", length = 100)
    private String spouseName;

    @Column(name = "alt_phone", length = 32)
    private String altPhone;

    @Column(name = "state_name", length = 100)
    private String stateName;

    @Column(length = 100)
    private String village;

    @Column(length = 16)
    private String pincode;

    @Column(name = "pan_no", length = 20)
    private String panNo;

    @Column(name = "aadhaar_no", length = 20)
    private String aadhaarNo;

    @Column(name = "country_name", length = 100)
    private String countryName = "India";

    // Extended KYC & Reference Fields
    @Column(length = 64)
    private String hos = "HOS";

    @Column(name = "account_group", length = 64)
    private String accountGroup = "Sundry Debtors";

    @Column(name = "relation_type", length = 16)
    private String relationType = "S/o";

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(length = 32)
    private String dob;

    @Column(name = "spouse_dob", length = 32)
    private String spouseDob;

    @Column(length = 64)
    private String caste;

    @Column(length = 100)
    private String qualification;

    @Column(name = "marital_status", length = 32)
    private String maritalStatus;

    @Column(name = "anniversary_date", length = 32)
    private String anniversaryDate;

    @Column(name = "tax_no", length = 64)
    private String taxNo;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_acc_name", length = 100)
    private String bankAccName;

    @Column(name = "bank_acc_no", length = 64)
    private String bankAccNo;

    @Column(name = "bank_ifsc_code", length = 32)
    private String bankIfscCode;

    @Column(length = 100)
    private String occupation;

    @Column(name = "annual_income", length = 64)
    private String annualIncome;

    @Column(name = "nominee_name", length = 100)
    private String nomineeName;

    @Column(name = "nominee_relation", length = 64)
    private String nomineeRelation;

    @Column(length = 100)
    private String designation;

    @Column(name = "login_id", length = 64)
    private String loginId;

    @Column(name = "portal_password", length = 128)
    private String portalPassword;

    @Column(name = "master_password", length = 128)
    private String masterPassword;

    @Column(name = "business_name", length = 255)
    private String businessName;

    @Column(name = "office_address", length = 500)
    private String officeAddress;

    @Column(name = "permanent_address", length = 500)
    private String permanentAddress;

    @Column(name = "current_address", length = 500)
    private String currentAddress;

    @Column(name = "shipping_address", length = 500)
    private String shippingAddress;

    @Column(name = "ward_number", length = 32)
    private String wardNumber;

    @Column(length = 100)
    private String tehsil;

    @Column(name = "other_info", length = 1000)
    private String otherInfo;

    @Column(name = "photo_base64", columnDefinition = "TEXT")
    private String photoBase64;

    @Column(name = "aadhaar_front_base64", columnDefinition = "TEXT")
    private String aadhaarFrontBase64;

    @Column(name = "aadhaar_front_desc", length = 255)
    private String aadhaarFrontDesc;

    @Column(name = "aadhaar_back_base64", columnDefinition = "TEXT")
    private String aadhaarBackBase64;

    @Column(name = "aadhaar_back_desc", length = 255)
    private String aadhaarBackDesc;

    @Column(name = "send_welcome_sms", nullable = false)
    private Boolean sendWelcomeSms = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Version
    private Long version;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPartyType() { return partyType; }
    public void setPartyType(String partyType) { this.partyType = partyType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getSpouseName() { return spouseName; }
    public void setSpouseName(String spouseName) { this.spouseName = spouseName; }
    public String getAltPhone() { return altPhone; }
    public void setAltPhone(String altPhone) { this.altPhone = altPhone; }
    public String getStateName() { return stateName; }
    public void setStateName(String stateName) { this.stateName = stateName; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getPanNo() { return panNo; }
    public void setPanNo(String panNo) { this.panNo = panNo; }
    public String getAadhaarNo() { return aadhaarNo; }
    public void setAadhaarNo(String aadhaarNo) { this.aadhaarNo = aadhaarNo; }
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getHos() { return hos; }
    public void setHos(String hos) { this.hos = hos; }
    public String getAccountGroup() { return accountGroup; }
    public void setAccountGroup(String accountGroup) { this.accountGroup = accountGroup; }
    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getSpouseDob() { return spouseDob; }
    public void setSpouseDob(String spouseDob) { this.spouseDob = spouseDob; }
    public String getCaste() { return caste; }
    public void setCaste(String caste) { this.caste = caste; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getMaritalStatus() { return maritalStatus; }
    public void setMaritalStatus(String maritalStatus) { this.maritalStatus = maritalStatus; }
    public String getAnniversaryDate() { return anniversaryDate; }
    public void setAnniversaryDate(String anniversaryDate) { this.anniversaryDate = anniversaryDate; }
    public String getTaxNo() { return taxNo; }
    public void setTaxNo(String taxNo) { this.taxNo = taxNo; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccName() { return bankAccName; }
    public void setBankAccName(String bankAccName) { this.bankAccName = bankAccName; }
    public String getBankAccNo() { return bankAccNo; }
    public void setBankAccNo(String bankAccNo) { this.bankAccNo = bankAccNo; }
    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String bankIfscCode) { this.bankIfscCode = bankIfscCode; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    public String getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(String annualIncome) { this.annualIncome = annualIncome; }
    public String getNomineeName() { return nomineeName; }
    public void setNomineeName(String nomineeName) { this.nomineeName = nomineeName; }
    public String getNomineeRelation() { return nomineeRelation; }
    public void setNomineeRelation(String nomineeRelation) { this.nomineeRelation = nomineeRelation; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }
    public String getPortalPassword() { return portalPassword; }
    public void setPortalPassword(String portalPassword) { this.portalPassword = portalPassword; }
    public String getMasterPassword() { return masterPassword; }
    public void setMasterPassword(String masterPassword) { this.masterPassword = masterPassword; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getOfficeAddress() { return officeAddress; }
    public void setOfficeAddress(String officeAddress) { this.officeAddress = officeAddress; }
    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }
    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getWardNumber() { return wardNumber; }
    public void setWardNumber(String wardNumber) { this.wardNumber = wardNumber; }
    public String getTehsil() { return tehsil; }
    public void setTehsil(String tehsil) { this.tehsil = tehsil; }
    public String getOtherInfo() { return otherInfo; }
    public void setOtherInfo(String otherInfo) { this.otherInfo = otherInfo; }
    public String getPhotoBase64() { return photoBase64; }
    public void setPhotoBase64(String photoBase64) { this.photoBase64 = photoBase64; }
    public String getAadhaarFrontBase64() { return aadhaarFrontBase64; }
    public void setAadhaarFrontBase64(String aadhaarFrontBase64) { this.aadhaarFrontBase64 = aadhaarFrontBase64; }
    public String getAadhaarFrontDesc() { return aadhaarFrontDesc; }
    public void setAadhaarFrontDesc(String aadhaarFrontDesc) { this.aadhaarFrontDesc = aadhaarFrontDesc; }
    public String getAadhaarBackBase64() { return aadhaarBackBase64; }
    public void setAadhaarBackBase64(String aadhaarBackBase64) { this.aadhaarBackBase64 = aadhaarBackBase64; }
    public String getAadhaarBackDesc() { return aadhaarBackDesc; }
    public void setAadhaarBackDesc(String aadhaarBackDesc) { this.aadhaarBackDesc = aadhaarBackDesc; }
    public Boolean getSendWelcomeSms() { return sendWelcomeSms; }
    public void setSendWelcomeSms(Boolean sendWelcomeSms) { this.sendWelcomeSms = sendWelcomeSms; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

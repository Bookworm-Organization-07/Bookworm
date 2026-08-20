package com.example.models;

import jakarta.persistence.*;

/**
 * A reusable royalty payee - a person, estate, or publisher - not tied
 * to any one product. Which products it currently receives royalty for
 * lives in BeneficiaryAssignment instead, so the same beneficiary can
 * be picked for any number of books.
 */
@Entity
@Table(name = "beneficiary")
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "beneficiary_id")
    private int beneficiaryId;

    @Column(name = "beneficiary_name", nullable = false)
    private String beneficiaryName;

    /** 'Author', 'Publisher', or 'Other' - free text, not an enum, so a new kind of payee needs no code change. */
    @Column(name = "beneficiary_type", nullable = false)
    private String beneficiaryType = "Other";

    @Column(name = "beneficiary_email_id")
    private String beneficiaryEmailId;

    @Column(name = "beneficiary_contact_no")
    private String beneficiaryContactNo;

    @Column(name = "beneficiary_bank_name")
    private String beneficiaryBankName;

    @Column(name = "beneficiary_bank_branch")
    private String beneficiaryBankBranch;

    @Column(name = "beneficiary_ifsc")
    private String beneficiaryIfsc;

    @Column(name = "beneficiary_acc_no")
    private String beneficiaryAccNo;

    @Column(name = "beneficiary_acc_type")
    private String beneficiaryAccType;

    @Column(name = "beneficiary_pan")
    private String beneficiaryPan;

    public Beneficiary() {
    }

    public int getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(int beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getBeneficiaryType() {
        return beneficiaryType;
    }

    public void setBeneficiaryType(String beneficiaryType) {
        this.beneficiaryType = beneficiaryType;
    }

    public String getBeneficiaryEmailId() {
        return beneficiaryEmailId;
    }

    public void setBeneficiaryEmailId(String beneficiaryEmailId) {
        this.beneficiaryEmailId = beneficiaryEmailId;
    }

    public String getBeneficiaryContactNo() {
        return beneficiaryContactNo;
    }

    public void setBeneficiaryContactNo(String beneficiaryContactNo) {
        this.beneficiaryContactNo = beneficiaryContactNo;
    }

    public String getBeneficiaryBankName() {
        return beneficiaryBankName;
    }

    public void setBeneficiaryBankName(String beneficiaryBankName) {
        this.beneficiaryBankName = beneficiaryBankName;
    }

    public String getBeneficiaryBankBranch() {
        return beneficiaryBankBranch;
    }

    public void setBeneficiaryBankBranch(String beneficiaryBankBranch) {
        this.beneficiaryBankBranch = beneficiaryBankBranch;
    }

    public String getBeneficiaryIfsc() {
        return beneficiaryIfsc;
    }

    public void setBeneficiaryIfsc(String beneficiaryIfsc) {
        this.beneficiaryIfsc = beneficiaryIfsc;
    }

    public String getBeneficiaryAccNo() {
        return beneficiaryAccNo;
    }

    public void setBeneficiaryAccNo(String beneficiaryAccNo) {
        this.beneficiaryAccNo = beneficiaryAccNo;
    }

    public String getBeneficiaryAccType() {
        return beneficiaryAccType;
    }

    public void setBeneficiaryAccType(String beneficiaryAccType) {
        this.beneficiaryAccType = beneficiaryAccType;
    }

    public String getBeneficiaryPan() {
        return beneficiaryPan;
    }

    public void setBeneficiaryPan(String beneficiaryPan) {
        this.beneficiaryPan = beneficiaryPan;
    }
}

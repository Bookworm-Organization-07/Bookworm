package com.example.Services;

import com.example.models.Beneficiary;

import java.util.List;

public interface BeneficiaryService {

    Beneficiary createBeneficiary(Beneficiary beneficiary);

    Beneficiary updateBeneficiary(int id, Beneficiary beneficiary);

    Beneficiary getBeneficiaryById(int id);

    List<Beneficiary> getAllBeneficiaries();

    void deleteBeneficiary(int id);
}

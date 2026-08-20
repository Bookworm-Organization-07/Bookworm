package com.example.Services;

import com.example.Repository.BeneficiaryRepository;
import com.example.models.Beneficiary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;

    public BeneficiaryServiceImpl(BeneficiaryRepository beneficiaryRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
    }

    @Override
    public Beneficiary createBeneficiary(Beneficiary beneficiary) {
        return beneficiaryRepository.save(beneficiary);
    }

    @Override
    public Beneficiary updateBeneficiary(int id, Beneficiary beneficiary) {
        Beneficiary existing = getBeneficiaryById(id);

        existing.setBeneficiaryName(beneficiary.getBeneficiaryName());
        existing.setBeneficiaryType(beneficiary.getBeneficiaryType());
        existing.setBeneficiaryEmailId(beneficiary.getBeneficiaryEmailId());
        existing.setBeneficiaryContactNo(beneficiary.getBeneficiaryContactNo());
        existing.setBeneficiaryBankName(beneficiary.getBeneficiaryBankName());
        existing.setBeneficiaryBankBranch(beneficiary.getBeneficiaryBankBranch());
        existing.setBeneficiaryIfsc(beneficiary.getBeneficiaryIfsc());
        existing.setBeneficiaryAccNo(beneficiary.getBeneficiaryAccNo());
        existing.setBeneficiaryAccType(beneficiary.getBeneficiaryAccType());
        existing.setBeneficiaryPan(beneficiary.getBeneficiaryPan());

        return beneficiaryRepository.save(existing);
    }

    @Override
    public Beneficiary getBeneficiaryById(int id) {
        return beneficiaryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found: " + id));
    }

    @Override
    public List<Beneficiary> getAllBeneficiaries() {
        return beneficiaryRepository.findAll();
    }

    @Override
    public void deleteBeneficiary(int id) {
        beneficiaryRepository.deleteById(id);
    }
}

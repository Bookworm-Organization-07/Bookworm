package com.example.Controller;

import com.example.Services.RoyaltyLedgerService;
import com.example.dto.RoyaltyLedgerEntryDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** The full royalty calculation history. Admin only - see SecurityConfig. */
@RestController
@RequestMapping("/api/royalty-ledger")
public class RoyaltyLedgerController {

    private final RoyaltyLedgerService royaltyLedgerService;

    public RoyaltyLedgerController(RoyaltyLedgerService royaltyLedgerService) {
        this.royaltyLedgerService = royaltyLedgerService;
    }

    @GetMapping
    public List<RoyaltyLedgerEntryDto> getLedger() {
        return royaltyLedgerService.getLedger();
    }
}

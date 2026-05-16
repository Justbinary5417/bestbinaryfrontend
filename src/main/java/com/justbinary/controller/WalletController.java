package com.justbinary.controller;

import com.justbinary.dto.DepositRequest;
import com.justbinary.dto.WithdrawRequest;
import com.justbinary.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/balance")
    public ResponseEntity<String> getBalance(Authentication authentication) {
        String email = authentication.getName();
        String result = walletService.getBalance(email);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest request,
                                          Authentication authentication) {
        String email = authentication.getName();
        String result = walletService.deposit(email, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest request,
                                           Authentication authentication) {
        String email = authentication.getName();
        String result = walletService.withdraw(email, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/transactions")
    public ResponseEntity<String> getTransactions(Authentication authentication) {
        String email = authentication.getName();
        String result = walletService.getTransactionHistory(email);
        return ResponseEntity.ok(result);
    }
}
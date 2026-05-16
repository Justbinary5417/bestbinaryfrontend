package com.justbinary.service;

import com.justbinary.dto.DepositRequest;
import com.justbinary.dto.WithdrawRequest;
import com.justbinary.model.*;
import com.justbinary.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final DepositRepository depositRepository;
    private final MpesaService mpesaService;

    public WalletServiceImpl(WalletRepository walletRepository,
                             UserRepository userRepository,
                             WithdrawalRepository withdrawalRepository,
                             DepositRepository depositRepository,
                             MpesaService mpesaService) {
        this.walletRepository     = walletRepository;
        this.userRepository       = userRepository;
        this.withdrawalRepository = withdrawalRepository;
        this.depositRepository    = depositRepository;
        this.mpesaService         = mpesaService;
    }

    @Override
    public String getBalance(String email) {
        User user = getUser(email);
        Wallet wallet = getWallet(user.getId());
        return "Balance: KES " + wallet.getBalance() 
             + " | Account: " + user.getAccountType();
    }

    @Override
    public String deposit(String email, DepositRequest request) {
        User user = getUser(email);

        // Block demo accounts
        if ("DEMO".equalsIgnoreCase(user.getAccountType())) {
            throw new RuntimeException(
                "Deposits are only allowed on REAL accounts. " +
                "Please upgrade your account to REAL.");
        }

        double amount = request.getAmount().doubleValue();
        if (amount < 10) {
            throw new RuntimeException("Minimum deposit is KES 10");
        }

        // Trigger M-Pesa STK Push
        String checkoutRequestId = mpesaService.stkPush(
            request.getPhoneNumber(), amount, "JustBinary Deposit"
        );

        // Save pending deposit record
        DepositRecord deposit = new DepositRecord();
        deposit.setUserId(user.getId());
        deposit.setAmount(amount);
        deposit.setPhoneNumber(request.getPhoneNumber());
        deposit.setMpesaCheckoutRequestId(checkoutRequestId);
        deposit.setStatus(DepositRecord.Status.PENDING);
        deposit.setRequestedAt(LocalDateTime.now());
        depositRepository.save(deposit);

        return "M-Pesa prompt sent to " + request.getPhoneNumber() 
             + ". Please enter your PIN to complete deposit of KES " + amount;
    }

    @Override
    public String withdraw(String email, WithdrawRequest request) {
        User user = getUser(email);

        // Block demo accounts
        if ("DEMO".equalsIgnoreCase(user.getAccountType())) {
            throw new RuntimeException(
                "Withdrawals are only allowed on REAL accounts. " +
                "Please upgrade your account to REAL.");
        }

        Wallet wallet = getWallet(user.getId());
        double amount = request.getAmount().doubleValue();

        if (wallet.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        if (amount < 100) {
            throw new RuntimeException("Minimum withdrawal is KES 100");
        }

        // Deduct balance and hold pending admin approval
        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // Save withdrawal record as PENDING for admin approval
        WithdrawalRecord record = new WithdrawalRecord();
        record.setUserId(user.getId());
        record.setAmount(amount);
        record.setPhoneNumber(request.getPhoneNumber());
        record.setMethod("MPESA");
        record.setStatus(WithdrawalRecord.Status.PENDING);
        record.setRequestedAt(LocalDateTime.now());
        withdrawalRepository.save(record);

        return "Withdrawal request of KES " + amount 
             + " submitted. Awaiting admin approval.";
    }

    @Override
    public String getTransactionHistory(String email) {
        User user = getUser(email);
        List<WithdrawalRecord> withdrawals = 
            withdrawalRepository.findByUserId(user.getId());
        List<DepositRecord> deposits = 
            depositRepository.findByUserId(user.getId());

        return "Deposits: " + deposits.toString() 
             + " | Withdrawals: " + withdrawals.toString();
    }

    // ── Helpers ──────────────────────────────────────
    private User getUser(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Wallet getWallet(String userId) {
        return walletRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
    }
}
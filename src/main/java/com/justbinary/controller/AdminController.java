package com.justbinary.controller;

import com.justbinary.dto.response.ApiResponse;
import com.justbinary.model.User;
import com.justbinary.model.Wallet;
import com.justbinary.model.WithdrawalRecord;
import com.justbinary.model.WithdrawalRecord.Status;
import com.justbinary.repository.UserRepository;
import com.justbinary.repository.WalletRepository;
import com.justbinary.repository.WithdrawalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WithdrawalRepository withdrawalRepository;

    public AdminController(UserRepository userRepository,
                           WalletRepository walletRepository,
                           WithdrawalRepository withdrawalRepository) {
        this.userRepository       = userRepository;
        this.walletRepository     = walletRepository;
        this.withdrawalRepository = withdrawalRepository;
    }

    // 1. GET ALL CLIENTS
    @GetMapping("/clients")
    public ResponseEntity<?> getAllClients() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> clientList = new ArrayList<>();

        for (User user : users) {
            Optional<Wallet> walletOpt = walletRepository.findByUserId(user.getId());
            Double balance = walletOpt.isPresent() ? walletOpt.get().getBalance() : 0.0;

            Map<String, Object> map = new HashMap<>();
            map.put("id",      user.getId());
            map.put("name",    user.getFullName());
            map.put("email",   user.getEmail());
            map.put("phone",   user.getPhone() != null ? user.getPhone() : "");
            map.put("balance", balance);
            map.put("status",  user.isEnabled() ? "active" : "inactive");
            clientList.add(map);
        }

        return ResponseEntity.ok(ApiResponse.success(clientList, "Clients fetched"));
    }

    // 2. ADD FUNDS TO A CLIENT
    @PostMapping("/clients/{id}/deposit")
    public ResponseEntity<?> addFunds(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {

        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Client not found", "NOT_FOUND"));
        }
        User user = userOpt.get();

        Optional<Wallet> walletOpt = walletRepository.findByUserId(id);
        if (!walletOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Wallet not found", "NOT_FOUND"));
        }
        Wallet wallet = walletOpt.get();

        Double amount = Double.parseDouble(body.get("amount").toString());
        if (amount <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Amount must be greater than zero", "INVALID_AMOUNT"));
        }

        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setTotalDeposited(wallet.getTotalDeposited() + amount);
        walletRepository.save(wallet);

        Map<String, Object> result = new HashMap<>();
        result.put("newBalance", wallet.getBalance());
        result.put("clientName", user.getFullName());

        return ResponseEntity.ok(ApiResponse.success(result, "Added $" + amount + " to " + user.getFullName()));
    }

    // 3. DEDUCT FUNDS FROM A CLIENT
    @PostMapping("/clients/{id}/deduct")
    public ResponseEntity<?> deductFunds(
            @PathVariable String id,
            @RequestBody Map<String, Object> body) {

        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Client not found", "NOT_FOUND"));
        }
        User user = userOpt.get();

        Optional<Wallet> walletOpt = walletRepository.findByUserId(id);
        if (!walletOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Wallet not found", "NOT_FOUND"));
        }
        Wallet wallet = walletOpt.get();

        Double amount = Double.parseDouble(body.get("amount").toString());
        if (amount <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Amount must be greater than zero", "INVALID_AMOUNT"));
        }
        if (wallet.getBalance() < amount) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Insufficient balance", "INSUFFICIENT_BALANCE"));
        }

        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn() + amount);
        walletRepository.save(wallet);

        Map<String, Object> result = new HashMap<>();
        result.put("newBalance", wallet.getBalance());
        result.put("clientName", user.getFullName());

        return ResponseEntity.ok(ApiResponse.success(result, "Deducted $" + amount + " from " + user.getFullName()));
    }

    // 4. REMOVE / DELETE A CLIENT
    @DeleteMapping("/clients/{id}")
    public ResponseEntity<?> removeClient(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Client not found", "NOT_FOUND"));
        }
        User user = userOpt.get();

        // Delete withdrawals first
        List<WithdrawalRecord> records = withdrawalRepository.findByUserId(id);
        withdrawalRepository.deleteAll(records);

        // Delete wallet
        Optional<Wallet> walletOpt = walletRepository.findByUserId(id);
        if (walletOpt.isPresent()) {
            walletRepository.delete(walletOpt.get());
        }

        userRepository.delete(user);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Client " + user.getFullName() + " removed successfully")
        );
    }

    // 5. GET ALL WITHDRAWALS
    @GetMapping("/withdrawals")
    public ResponseEntity<?> getAllWithdrawals() {
        List<WithdrawalRecord> records = withdrawalRepository.findAllByOrderByRequestedAtDesc();
        List<Map<String, Object>> result = new ArrayList<>();

        for (WithdrawalRecord wr : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",          wr.getId());
            map.put("clientId",    wr.getUserId());
            map.put("amount",      wr.getAmount());
            map.put("phoneNumber", wr.getPhoneNumber());
            map.put("method",      wr.getMethod());
            map.put("status",      wr.getStatus().name());
            map.put("requestedAt", wr.getRequestedAt().toString());
            result.add(map);
        }

        return ResponseEntity.ok(ApiResponse.success(result, "Withdrawals fetched"));
    }

    // 6. GET PENDING WITHDRAWALS
    @GetMapping("/withdrawals/pending")
    public ResponseEntity<?> getPendingWithdrawals() {
        List<WithdrawalRecord> records = withdrawalRepository.findByStatus(Status.PENDING);
        List<Map<String, Object>> result = new ArrayList<>();

        for (WithdrawalRecord wr : records) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",          wr.getId());
            map.put("clientId",    wr.getUserId());
            map.put("amount",      wr.getAmount());
            map.put("phoneNumber", wr.getPhoneNumber());
            map.put("method",      wr.getMethod());
            map.put("status",      wr.getStatus().name());
            map.put("requestedAt", wr.getRequestedAt().toString());
            result.add(map);
        }

        return ResponseEntity.ok(ApiResponse.success(result, "Pending withdrawals fetched"));
    }

    // 7. APPROVE A WITHDRAWAL
    @PostMapping("/withdrawals/{withdrawalId}/approve")
    public ResponseEntity<?> approveWithdrawal(@PathVariable String withdrawalId) {
        Optional<WithdrawalRecord> wrOpt = withdrawalRepository.findById(withdrawalId);
        if (!wrOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Withdrawal not found", "NOT_FOUND"));
        }
        WithdrawalRecord wr = wrOpt.get();

        if (wr.getStatus() != Status.PENDING) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Withdrawal already resolved", "ALREADY_RESOLVED"));
        }

        Optional<Wallet> walletOpt = walletRepository.findByUserId(wr.getUserId());
        if (!walletOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Client wallet not found", "NOT_FOUND"));
        }
        Wallet wallet = walletOpt.get();

        if (wallet.getBalance() < wr.getAmount()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Client has insufficient balance", "INSUFFICIENT_BALANCE"));
        }

        wallet.setBalance(wallet.getBalance() - wr.getAmount());
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn() + wr.getAmount());
        walletRepository.save(wallet);

        wr.setStatus(Status.APPROVED);
        wr.setResolvedAt(LocalDateTime.now());
        withdrawalRepository.save(wr);

        Map<String, Object> result = new HashMap<>();
        result.put("withdrawalId", wr.getId());
        result.put("amount",       wr.getAmount());
        result.put("newBalance",   wallet.getBalance());

        return ResponseEntity.ok(ApiResponse.success(result, "Withdrawal approved successfully"));
    }

    // 8. DENY A WITHDRAWAL
    @PostMapping("/withdrawals/{withdrawalId}/deny")
    public ResponseEntity<?> denyWithdrawal(@PathVariable String withdrawalId) {
        Optional<WithdrawalRecord> wrOpt = withdrawalRepository.findById(withdrawalId);
        if (!wrOpt.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Withdrawal not found", "NOT_FOUND"));
        }
        WithdrawalRecord wr = wrOpt.get();

        if (wr.getStatus() != Status.PENDING) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Withdrawal already resolved", "ALREADY_RESOLVED"));
        }

        wr.setStatus(Status.DENIED);
        wr.setResolvedAt(LocalDateTime.now());
        withdrawalRepository.save(wr);

        return ResponseEntity.ok(
                ApiResponse.success(null, "Withdrawal denied successfully")
        );
    }

    // 9. DASHBOARD STATS
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        List<User> allUsers = userRepository.findAll();
        long total    = allUsers.size();
        long inactive = 0;
        double totalBalance = 0.0;

        for (User user : allUsers) {
            if (!user.isEnabled()) inactive++;
            Optional<Wallet> walletOpt = walletRepository.findByUserId(user.getId());
            if (walletOpt.isPresent()) {
                totalBalance += walletOpt.get().getBalance();
            }
        }

        long pendingCount = withdrawalRepository.findByStatus(Status.PENDING).size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClients",       total);
        stats.put("inactiveClients",    inactive);
        stats.put("totalBalance",       totalBalance);
        stats.put("pendingWithdrawals", pendingCount);

        return ResponseEntity.ok(ApiResponse.success(stats, "Stats fetched"));
    }
}
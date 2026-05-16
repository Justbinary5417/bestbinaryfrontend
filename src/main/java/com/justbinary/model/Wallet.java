package com.justbinary.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "wallets")
public class Wallet {

    @Id
    private String id;

    private String userId;

    private Double balance = 0.0;
    private Double totalDeposited = 0.0;
    private Double totalWithdrawn = 0.0;
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ===== CONSTRUCTORS =====
    public Wallet() {}

    // ===== GETTERS =====
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public Double getBalance() { return balance; }
    public Double getTotalDeposited() { return totalDeposited; }
    public Double getTotalWithdrawn() { return totalWithdrawn; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ===== SETTERS =====
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setBalance(Double balance) { this.balance = balance; }
    public void setTotalDeposited(Double totalDeposited) { this.totalDeposited = totalDeposited; }
    public void setTotalWithdrawn(Double totalWithdrawn) { this.totalWithdrawn = totalWithdrawn; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
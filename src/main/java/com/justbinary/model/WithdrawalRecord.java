package com.justbinary.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "withdrawal_records")
public class WithdrawalRecord {

    public enum Status {
        PENDING, APPROVED, DENIED
    }

    @Id
    private String id;

    private String userId;

    private Double amount;
    private String phoneNumber;
    private String method;
    private Status status = Status.PENDING;
    private LocalDateTime requestedAt = LocalDateTime.now();
    private LocalDateTime resolvedAt;

    // ===== CONSTRUCTORS =====
    public WithdrawalRecord() {}

    public WithdrawalRecord(String userId, Double amount, String phoneNumber, String method) {
        this.userId      = userId;
        this.amount      = amount;
        this.phoneNumber = phoneNumber;
        this.method      = method;
        this.status      = Status.PENDING;
        this.requestedAt = LocalDateTime.now();
    }

    // ===== GETTERS =====
    public String getId()                { return id; }
    public String getUserId()            { return userId; }
    public Double getAmount()            { return amount; }
    public String getPhoneNumber()       { return phoneNumber; }
    public String getMethod()            { return method; }
    public Status getStatus()            { return status; }
    public LocalDateTime getRequestedAt(){ return requestedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }

    // ===== SETTERS =====
    public void setId(String id)                         { this.id = id; }
    public void setUserId(String userId)                 { this.userId = userId; }
    public void setAmount(Double amount)                 { this.amount = amount; }
    public void setPhoneNumber(String phoneNumber)       { this.phoneNumber = phoneNumber; }
    public void setMethod(String method)                 { this.method = method; }
    public void setStatus(Status status)                 { this.status = status; }
    public void setRequestedAt(LocalDateTime requestedAt){ this.requestedAt = requestedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt)  { this.resolvedAt = resolvedAt; }
}
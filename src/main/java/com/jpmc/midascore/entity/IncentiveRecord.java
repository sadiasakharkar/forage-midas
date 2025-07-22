package com.jpmc.midascore.entity;

import jakarta.persistence.*;

@Entity
public class IncentiveRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Double amount;

    public IncentiveRecord() {
    }

    public IncentiveRecord(Long userId, Double amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}

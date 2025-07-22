package com.jpmc.midascore.foundation;

public class Balance {
    private double amount;

    public Balance() {
        this.amount = 0.0;
    }

    public Balance(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Balance {amount=" + amount + "}";
    }
}

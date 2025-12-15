package com.tymex.model;

public class PaymentResponse {
    public String transactionId;
    public String status;
    public long amount;

    public PaymentResponse(String transactionId, String status, long amount) {
        this.transactionId = transactionId;
        this.status = status;
        this.amount = amount;
    }
}
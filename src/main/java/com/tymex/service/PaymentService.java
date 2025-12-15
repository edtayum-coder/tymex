package com.tymex.service;

import com.tymex.model.PaymentRequest;
import com.tymex.model.PaymentResponse;
import com.tymex.store.InMemoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class PaymentService {

    private final InMemoryRepository repository;

    // TTL in seconds (e.g., 3600 = 1 hour)
    private final long ttlSeconds = 3600L;

    public PaymentService(InMemoryRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity<PaymentResponse> processPayment(String idemKey, PaymentRequest request) {
        if (idemKey == null || idemKey.isBlank()) {
            // No idempotency key provided — still process normally (not stored)
            PaymentResponse resp = callPaymentProvider(request);
            return ResponseEntity.ok(resp);
        }

        final String key = idemKey.trim();

        // Try to create or retrieve a record atomically. We use computeIfAbsent to insert a placeholder
        CompletableFuture<ResponseEntity<PaymentResponse>> future = repository.computeIfAbsentFuture(key, () -> {
            // create a future that will be completed when processing finishes
            CompletableFuture<ResponseEntity<PaymentResponse>> f = new CompletableFuture<>();
            // process async in separate thread to avoid blocking the map lock for long
            CompletableFuture.runAsync(() -> {
                try {
                    PaymentResponse resp = callPaymentProvider(request);
                    ResponseEntity<PaymentResponse> entity = ResponseEntity.ok(resp);
                    repository.saveResponse(key, entity, Instant.now().getEpochSecond(), ttlSeconds);
                    f.complete(entity);
                } catch (Exception ex) {
                    f.completeExceptionally(ex);
                }
            });
            return f;
        });

        try {
            // Wait for completion (other concurrent callers will wait here)
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).build();
        } catch (ExecutionException e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Simulated payment provider call — in real life replace with real integration
    protected PaymentResponse callPaymentProvider(PaymentRequest request) {
        // Simulate deterministic response using request + random transaction id
        String tx = UUID.randomUUID().toString();
        return new PaymentResponse(tx, "SUCCESS", request.amount);
    }
}
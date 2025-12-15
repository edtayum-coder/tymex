package com.tymex.store;

import com.tymex.model.PaymentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class InMemoryRepository {

    private static class Record {
        final CompletableFuture<ResponseEntity<PaymentResponse>> future;
        volatile long createdAtEpochSec;
        volatile long ttlSeconds;

        Record(CompletableFuture<ResponseEntity<PaymentResponse>> future, long createdAtEpochSec, long ttlSeconds) {
            this.future = future;
            this.createdAtEpochSec = createdAtEpochSec;
            this.ttlSeconds = ttlSeconds;
        }

        boolean isExpired(long nowSec) {
            return nowSec - createdAtEpochSec > ttlSeconds;
        }
    }

    private final Map<String, Record> map = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public InMemoryRepository() {
        // run cleaner every 1 minute
        cleaner.scheduleAtFixedRate(this::cleanup, 1, 1, TimeUnit.MINUTES);
    }

    public CompletableFuture<ResponseEntity<PaymentResponse>> computeIfAbsentFuture(String key, java.util.function.Supplier<CompletableFuture<ResponseEntity<PaymentResponse>>> supplier) {
        Objects.requireNonNull(key);
        long now = Instant.now().getEpochSecond();

        while (true) {
            Record existing = map.get(key);
            if (existing != null) {
                if (existing.isExpired(now)) {
                    // try remove stale
                    map.remove(key, existing);
                    continue; // try again
                }
                return existing.future;
            }

            CompletableFuture<ResponseEntity<PaymentResponse>> future = supplier.get();
            Record rec = new Record(future, now, 3600);
            Record prev = map.putIfAbsent(key, rec);
            if (prev == null) {
                return future;
            } else {
                // concurrent insert — use existing
                if (!prev.isExpired(now)) {
                    return prev.future;
                } else {
                    map.remove(key, prev);
                }
            }
        }
    }

    public void saveResponse(String key, ResponseEntity<PaymentResponse> response, long createdAtEpochSec, long ttlSeconds) {
        CompletableFuture<ResponseEntity<PaymentResponse>> future = CompletableFuture.completedFuture(response);
        Record rec = new Record(future, createdAtEpochSec, ttlSeconds);
        map.put(key, rec);
    }

    private void cleanup() {
        long now = Instant.now().getEpochSecond();
        for (Map.Entry<String, Record> e : map.entrySet()) {
            if (e.getValue().isExpired(now)) {
                map.remove(e.getKey(), e.getValue());
            }
        }
    }
}

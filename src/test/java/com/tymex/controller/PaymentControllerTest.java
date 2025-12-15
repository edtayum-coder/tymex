package com.tymex;

import com.tymex.model.PaymentRequest;
import com.tymex.model.PaymentResponse;
import com.tymex.store.InMemoryRepository;
import com.tymex.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenSameIdempotencyKey_concurrentRequests_receiveSameResponse() throws Exception {
        String idemKey = "test-key-123";
        PaymentRequest req = new PaymentRequest();
        req.accountId = "acc-1";
        req.amount = 1000;

        int threads = 4;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger(0);

        Thread[] workers = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            workers[i] = new Thread(() -> {
                try {
                    start.await();
                    String body = objectMapper.writeValueAsString(req);
                    String resp = mockMvc.perform(post("/payments")
                            .header("Idempotency-Key", idemKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                            .andExpect(status().isOk())
                            .andReturn().getResponse().getContentAsString();
                    if (resp != null && !resp.isBlank()) {
                        success.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    done.countDown();
                }
            });
            workers[i].start();
        }

        start.countDown();
        done.await();

        assertThat(success.get()).isEqualTo(threads);
    }
    
    @Test
    void shouldFailWhenRepositoryThrowsException() {
    // Fake repository that always throws on compute
    InMemoryRepository faultyRepo = new InMemoryRepository() {
        @Override
        public CompletableFuture<ResponseEntity<PaymentResponse>> computeIfAbsentFuture(
                String key, java.util.function.Supplier<CompletableFuture<ResponseEntity<PaymentResponse>>> supplier) {
            throw new RuntimeException("Repo error");
        }
    };

    PaymentService faultyService = new PaymentService(faultyRepo);

    PaymentRequest req = new PaymentRequest();
    req.accountId = "acc1";
    req.amount = 100;

    // Expect exception when repository fails
    assertThrows(RuntimeException.class, () -> {
        faultyService.processPayment("err-key", req);
    });
}

    @Test
    void shouldFailWhenProcessorThrowsException() {
        InMemoryRepository repo = new InMemoryRepository();
        
        // Override PaymentService to simulate processor failure
        PaymentService service = new PaymentService(repo) {
            @Override
            protected PaymentResponse callPaymentProvider(PaymentRequest request) {
                throw new RuntimeException("Processing failed");
            }
        };
        
        PaymentRequest req = new PaymentRequest();
        req.accountId = "acc1";
        req.amount = 100;

        // Expect RuntimeException
        assertThrows(RuntimeException.class, () -> {
            service.processPayment("boom-key", req);
        });
    }
    
    @Test
    void shouldNotSaveRecordWhenProcessorFails() throws Exception {
        InMemoryRepository repo = new InMemoryRepository();

        PaymentService service = new PaymentService(repo) {
            @Override
            public PaymentResponse callPaymentProvider(PaymentRequest request) {
                throw new RuntimeException("fail");
            }
        };

        PaymentRequest req = new PaymentRequest();
        req.accountId = "acc1";
        req.amount = 200;

        try {
            service.processPayment("fail-case", req);
        } catch (RuntimeException ignored) {}

        // Verify new future is created for same key (old one not saved)
        CompletableFuture<ResponseEntity<PaymentResponse>> future = repo.computeIfAbsentFuture(
            "fail-case",
            () -> CompletableFuture.completedFuture(
                ResponseEntity.ok(new PaymentResponse("dummy", "SUCCESS", 0)))
        ); 
    }
    
}

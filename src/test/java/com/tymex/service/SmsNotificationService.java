package com.tymex.service;

import org.junit.jupiter.api.Test;

class SmsNotificationServiceTest {

    @Test
    void shouldSendSmsWithoutError() {
        SmsNotificationService service = new SmsNotificationService();

        service.send("123", "Hello SMS");
    }
}
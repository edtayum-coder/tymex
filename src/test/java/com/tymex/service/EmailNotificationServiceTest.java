package com.tymex.service;

import org.junit.jupiter.api.Test;

class EmailNotificationServiceTest {

    @Test
    void shouldSendEmailWithoutError() {
        EmailNotificationService service = new EmailNotificationService();

        service.send("123", "Hello Email");
    }
}
package com.tymex.service;

import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    @Override
    public void send(String userId, String message) {
        // Fake 3rd party call
        System.out.println("Sending EMAIL to user " + userId + ": " + message);
    }
}
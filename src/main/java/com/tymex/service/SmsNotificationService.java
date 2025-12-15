package com.tymex.service;

import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService implements NotificationService {

    @Override
    public void send(String userId, String message) {
        // Let's mock the 3rd party call
        System.out.println("Sending SMS to user " + userId + ": " + message);
    }
}
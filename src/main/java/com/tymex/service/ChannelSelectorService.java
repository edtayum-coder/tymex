package com.tymex.service;

import com.tymex.model.UserPreference;
import org.springframework.stereotype.Service;

@Service
public class ChannelSelectorService {

    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;

    public ChannelSelectorService(EmailNotificationService emailService,
                                  SmsNotificationService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }

    public void notifyUser(String userId, String message, UserPreference preference) {

        if (preference.isEmailEnabled()) {
            emailService.send(userId, message);
        } else if (preference.isSmsEnabled()) {
            smsService.send(userId, message);
        } else {
            System.out.println("No notification channel enabled for user " + userId);
        }
    }
}
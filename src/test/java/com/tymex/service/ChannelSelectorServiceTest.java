package com.tymex.service;

import com.tymex.model.UserPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ChannelSelectorServiceTest {

    private EmailNotificationService emailService;
    private SmsNotificationService smsService;
    private ChannelSelectorService channelSelectorService;

    @BeforeEach
    void setup() {
        emailService = Mockito.mock(EmailNotificationService.class);
        smsService = Mockito.mock(SmsNotificationService.class);
        channelSelectorService = new ChannelSelectorService(emailService, smsService);
    }

    @Test
    void shouldSendEmailWhenEmailEnabled() {
        UserPreference pref = new UserPreference(true, false);

        channelSelectorService.notifyUser("123", "Hello", pref);

        Mockito.verify(emailService, Mockito.times(1))
                .send("123", "Hello");

        Mockito.verify(smsService, Mockito.never())
                .send(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void shouldSendSmsWhenEmailDisabledAndSmsEnabled() {
        UserPreference pref = new UserPreference(false, true);

        channelSelectorService.notifyUser("123", "Hello", pref);

        Mockito.verify(smsService, Mockito.times(1))
                .send("123", "Hello");

        Mockito.verify(emailService, Mockito.never())
                .send(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void shouldNotSendAnythingWhenNoChannelsEnabled() {
        UserPreference pref = new UserPreference(false, false);

        channelSelectorService.notifyUser("123", "Hello", pref);

        Mockito.verify(emailService, Mockito.never())
                .send(Mockito.anyString(), Mockito.anyString());

        Mockito.verify(smsService, Mockito.never())
                .send(Mockito.anyString(), Mockito.anyString());
    }
}
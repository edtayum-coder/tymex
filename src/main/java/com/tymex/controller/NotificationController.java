package com.tymex.controller;

import com.tymex.model.NotificationRequest;
import com.tymex.model.UserPreference;
import com.tymex.service.ChannelSelectorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notify")
public class NotificationController {

    private final ChannelSelectorService channelSelectorService;

    public NotificationController(ChannelSelectorService service) {
        this.channelSelectorService = service;
    }

    @PostMapping
    public String sendNotification(@RequestBody NotificationRequest request) {

        // For demo, hardcode. In real apps: fetch from DB
        UserPreference preference = new UserPreference(true, false); 
        // means: email=ON, sms=OFF

        channelSelectorService.notifyUser(
                request.getUserId(),
                request.getMessage(),
                preference
        );

        return "Notification Sent";
    }
}
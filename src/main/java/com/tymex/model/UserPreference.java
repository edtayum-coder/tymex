package com.tymex.model;

public class UserPreference {

    private boolean emailEnabled;
    private boolean smsEnabled;

    public UserPreference() {}

    public UserPreference(boolean emailEnabled, boolean smsEnabled) {
        this.emailEnabled = emailEnabled;
        this.smsEnabled = smsEnabled;
    }

    public boolean isEmailEnabled() { return emailEnabled; }

    public boolean isSmsEnabled() { return smsEnabled; }
}
package com.tride.tridewaitlist.service;

import com.tride.tridewaitlist.model.Waitlist;

public interface WaitlistService {
    boolean emailExists(String email);
    void addToWaitlist(Waitlist waitlist);
    boolean isValidEmail(String email);
}
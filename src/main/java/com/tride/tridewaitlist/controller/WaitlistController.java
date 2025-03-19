package com.tride.tridewaitlist.controller;

import com.tride.tridewaitlist.model.Waitlist;
import com.tride.tridewaitlist.service.WaitlistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/waitlist")
@Slf4j
public class WaitlistController {

    private final WaitlistService waitlistService;

    @Autowired
    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addToWaitlist(@RequestBody Waitlist waitlist) {
//        if (waitlistService.emailExists(waitlist.getEmail())) {
//            return ResponseEntity.status(HttpStatus.CONFLICT)
//                    .body(Map.of("Status", "409",
//                            "Message", "Email already exists in waitlist."));
//        }

        if (!waitlistService.isValidEmail(waitlist.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("Status", "400",
                            "Message", "Invalid email format. Please provide a valid email address."));
        }

        waitlistService.addToWaitlist(waitlist);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("Status", "200",
                        "Message", "Added to waitlist successfully!"));
    }
}
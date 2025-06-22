package com.tride.tridewaitlist.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(WaitlistServiceImpl.class);

    private final RestTemplate restTemplate;
    private final String resendApiKey;

    @Autowired
    public EmailService(RestTemplate restTemplate, @Value("${resend.api-key}") String resendApiKey) {
        this.restTemplate = restTemplate;
        this.resendApiKey = resendApiKey;
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        String url = "https://api.resend.com/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + resendApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("from", "hello@tridepay.africa");
        requestBody.put("to", to);
        requestBody.put("subject", subject);
        requestBody.put("html", htmlContent);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            log.info("Email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Resend email request failed: " + e.getMessage(), e);
        }
    }
}
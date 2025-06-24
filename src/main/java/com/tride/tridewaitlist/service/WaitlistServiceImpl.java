package com.tride.tridewaitlist.service;

import com.tride.tridewaitlist.model.Waitlist;
import com.tride.tridewaitlist.repository.WaitlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class WaitlistServiceImpl implements WaitlistService {
    private static final Logger log = LoggerFactory.getLogger(WaitlistServiceImpl.class);
    private final WaitlistRepository waitlistRepository;
    private final EmailService emailService;
    private final RestTemplate restTemplate;
    private final String airtableApiKey;
    private final String airtableBaseId;
    private final String airtableTableName;

    public WaitlistServiceImpl(
            WaitlistRepository waitlistRepository, EmailService emailService,
            RestTemplate restTemplate,
            @Value("${airtable.api-key}") String airtableApiKey,
            @Value("${airtable.base-id}") String airtableBaseId,
            @Value("${airtable.table-name}") String airtableTableName) {
        this.waitlistRepository = waitlistRepository;
        this.emailService = emailService;
        this.restTemplate = restTemplate;
        this.airtableApiKey = airtableApiKey;
        this.airtableBaseId = airtableBaseId;
        this.airtableTableName = airtableTableName;
    }

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    @Override
    public void addToWaitlist(Waitlist waitlist) {
        if (!isValidEmail(waitlist.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        waitlist.setJoinDate(LocalDateTime.now());
        waitlistRepository.save(waitlist);

        try {
            String url = String.format("https://api.airtable.com/v0/%s/%s", airtableBaseId.trim(), airtableTableName.trim());
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + airtableApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
            String formattedJoinDate = waitlist.getJoinDate().format(formatter);

            Map<String, Object> fields = new HashMap<>();
            fields.put("Full Name", waitlist.getFullName());
            fields.put("Email", waitlist.getEmail());
            fields.put("Join Date", formattedJoinDate);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("fields", fields);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            log.info("Successfully added to Airtable: {}", waitlist.getEmail());
        } catch (Exception e) {
            log.error("Failed to send to Airtable for email {}: {}", waitlist.getEmail(), e.getMessage());
            throw new RuntimeException("Airtable request failed: " + e.getMessage(), e);
        }

        sendWaitlistConfirmationEmail(waitlist.getEmail(), waitlist.getFullName());
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    private void sendWaitlistConfirmationEmail(String email, String fullName) {
        String subject = "Welcome to Our Waitlist!";
        String htmlTemplate =
                "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "  <meta charset=\"UTF-8\">\n" +
                        "  <title>Welcome to TridePay</title>\n" +
                        "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>\n" +
                        "  <style>\n" +
                        "    body, table, td { \n" +
                        "      margin: 0; \n" +
                        "      padding: 0; \n" +
                        "      border: 0; \n" +
                        "      font-family: Arial, Helvetica, sans-serif; \n" +
                        "      -webkit-font-smoothing: antialiased;\n" +
                        "      -moz-osx-font-smoothing: grayscale;\n" +
                        "    }\n" +
                        "    .wrapper { \n" +
                        "      width: 100%; \n" +
                        "      background: linear-gradient(135deg, #2c5282 0%, #93c5fd 50%, #1a202c 100%);\n" +
                        "      min-height: 100vh;\n" +
                        "      padding: 60px 20px; \n" +
                        "    }\n" +
                        "    .container { \n" +
                        "      max-width: 640px; \n" +
                        "      width: 100%; \n" +
                        "      margin: 0 auto; \n" +
                        "      background: #ffffff;\n" +
                        "      border-radius: 24px; \n" +
                        "      overflow: hidden; \n" +
                        "      box-shadow: 0 20px 40px rgba(26, 32, 44, 0.2);\n" +
                        "    }\n" +
                        "    .header { \n" +
                        "      background: linear-gradient(135deg, #2c5282 0%, #1a202c 100%);\n" +
                        "      text-align: center; \n" +
                        "      padding: 50px 30px; \n" +
                        "      position: relative;\n" +
                        "      overflow: hidden;\n" +
                        "    }\n" +
                        "    .header::before {\n" +
                        "      content: '';\n" +
                        "      position: absolute;\n" +
                        "      top: -50%;\n" +
                        "      right: -50%;\n" +
                        "      width: 200%;\n" +
                        "      height: 200%;\n" +
                        "      background: radial-gradient(circle, rgba(147, 197, 253, 0.1) 0%, transparent 70%);\n" +
                        "      pointer-events: none;\n" +
                        "    }\n" +
                        "    .header-content { \n" +
                        "      width: 100%; \n" +
                        "      text-align: center; \n" +
                        "      position: relative;\n" +
                        "      z-index: 2;\n" +
                        "    }\n" +
                        "    .header h1 { \n" +
                        "      color: #ffffff; \n" +
                        "      font-size: 32px; \n" +
                        "      margin: 0; \n" +
                        "      font-weight: bold; \n" +
                        "      letter-spacing: -0.5px; \n" +
                        "      display: inline-block; \n" +
                        "      vertical-align: middle; \n" +
                        "      padding-left: 16px; \n" +
                        "      text-shadow: 0 2px 4px rgba(0,0,0,0.3);\n" +
                        "    }\n" +
                        "    .body { \n" +
                        "      padding: 50px 40px; \n" +
                        "      color: #1a202c; \n" +
                        "      line-height: 1.7; \n" +
                        "      background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%); \n" +
                        "    }\n" +
                        "    .body p { \n" +
                        "      margin-bottom: 20px; \n" +
                        "      font-size: 18px; \n" +
                        "      text-align: center; \n" +
                        "      color: #2c5282; \n" +
                        "      max-width: 600px; \n" +
                        "      margin-left: auto; \n" +
                        "      margin-right: auto; \n" +
                        "    }\n" +
                        "    .highlight-text {\n" +
                        "      color: #1a202c;\n" +
                        "      font-weight: 600;\n" +
                        "    }\n" +
                        "    .signature { \n" +
                        "      padding: 40px; \n" +
                        "      background: linear-gradient(135deg, #93c5fd 0%, #dbeafe 100%); \n" +
                        "      border-top: 1px solid rgba(44, 82, 130, 0.1); \n" +
                        "    }\n" +
                        "    .signature p { \n" +
                        "      margin: 0; \n" +
                        "      text-align: center; \n" +
                        "      color: #1a202c; \n" +
                        "      font-size: 16px; \n" +
                        "      font-weight: 500;\n" +
                        "    }\n" +
                        "    .footer { \n" +
                        "      background: linear-gradient(135deg, #1a202c 0%, #2c5282 100%); \n" +
                        "      text-align: center; \n" +
                        "      padding: 40px 30px; \n" +
                        "      font-size: 14px; \n" +
                        "      color: #93c5fd; \n" +
                        "    }\n" +
                        "    .footer a { \n" +
                        "      margin: 0 8px; \n" +
                        "      color: #93c5fd; \n" +
                        "      text-decoration: none; \n" +
                        "      padding: 8px 16px; \n" +
                        "      border-radius: 8px; \n" +
                        "      font-weight: 500; \n" +
                        "      border: 1px solid rgba(147, 197, 253, 0.3);\n" +
                        "      transition: all 0.3s ease;\n" +
                        "    }\n" +
                        "    .footer a:hover {\n" +
                        "      background-color: rgba(147, 197, 253, 0.1);\n" +
                        "      border-color: #93c5fd;\n" +
                        "    }\n" +
                        "    .footer p { \n" +
                        "      margin: 20px 0 0; \n" +
                        "      font-size: 13px; \n" +
                        "      opacity: 0.8; \n" +
                        "      color: #93c5fd;\n" +
                        "    }\n" +
                        "    @media only screen and (max-width: 480px) {\n" +
                        "      .wrapper { padding: 20px 10px !important; }\n" +
                        "      .body, .signature { padding: 30px 25px !important; }\n" +
                        "      .header { padding: 40px 20px !important; }\n" +
                        "      .header h1 { font-size: 26px !important; }\n" +
                        "      .body p { font-size: 16px !important; }\n" +
                        "      .footer { padding: 30px 20px !important; }\n" +
                        "      .footer a { display: inline-block; margin: 4px 6px !important; }\n" +
                        "    }\n" +
                        "  </style>\n" +
                        "</head>\n" +
                        "<body class=\"wrapper\">\n" +
                        "  <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;\">\n" +
                        "    <tr><td align=\"center\">\n" +
                        "      <table class=\"container\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse:collapse;\">\n" +
                        "        <tr><td class=\"header\" align=\"center\">\n" +
                        "          <div class=\"header-content\">\n" +
                        "            <table cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse; margin: 0 auto;\">\n" +
                        "              <tr>\n" +
                        "                <td style=\"vertical-align: middle; padding-right: 12px;\">\n" +
                        "                  <table cellpadding=\"0\" cellspacing=\"0\" style=\"border-collapse: collapse; position: relative; width: 48px; height: 48px;\">\n" +
                        "                    <tr>\n" +
                        "                      <td style=\"position: relative; width: 48px; height: 48px;\">\n" +
                        "                        <!-- Card 1 (bottom - dark navy/black) -->\n" +
                        "                        <div style=\"width: 32px; height: 20px; background-color: #1a202c; border-radius: 6px; position: absolute; top: 28px; left: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.4); z-index: 1; border: 1px solid rgba(147, 197, 253, 0.2);\"></div>\n" +
                        "                        <!-- Card 2 (middle - light blue) -->\n" +
                        "                        <div style=\"width: 32px; height: 20px; background-color: #93c5fd; border-radius: 6px; position: absolute; top: 20px; left: 4px; box-shadow: 0 4px 8px rgba(0,0,0,0.3); z-index: 2; border: 1px solid rgba(255, 255, 255, 0.3);\"></div>\n" +
                        "                        <!-- Card 3 (top - dark blue) -->\n" +
                        "                        <div style=\"width: 32px; height: 20px; background-color: #2c5282; border-radius: 6px; position: absolute; top: 12px; left: 0px; box-shadow: 0 4px 8px rgba(0,0,0,0.4); z-index: 3; border: 1px solid rgba(147, 197, 253, 0.3);\"></div>\n" +
                        "                      </td>\n" +
                        "                    </tr>\n" +
                        "                  </table>\n" +
                        "                </td>\n" +
                        "                <td style=\"vertical-align: middle;\">\n" +
                        "                  <h1 style=\"color: #ffffff; font-size: 32px; margin: 0; font-weight: bold; font-family: Arial, Helvetica, sans-serif; letter-spacing: -0.5px; text-shadow: 0 2px 4px rgba(0,0,0,0.3);\">TridePay</h1>\n" +
                        "                </td>\n" +
                        "              </tr>\n" +
                        "            </table>\n" +
                        "          </div>\n" +
                        "        </td></tr>\n" +
                        "        <tr><td class=\"body\">\n" +
                        "          <p>Hey there! 👋 Welcome to <span class=\"highlight-text\">TridePay</span>.</p>\n" +
                        "          <p>We're super excited to have you on the waitlist!</p>\n" +
                        "          <p><span class=\"highlight-text\">TridePay</span> is here to help you stay on top of your money — from tracking your spending to setting budgets that actually work for you. No stress, no confusing tools. Just simple, smart money management.</p>\n" +
                        "          <p>We'll keep you in the loop and let you know the moment we're ready for you to jump in.</p>\n" +
                        "          <p>Thanks for joining us early — let's make money management feel good.</p>\n" +
                        "        </td></tr>\n" +
                        "        <tr><td class=\"signature\">\n" +
                        "          <p>— Your friends at TridePay 💙</p>\n" +
                        "        </td></tr>\n" +
                        "        <tr><td class=\"footer\">\n" +
                        "          <a href=\"#\">Privacy Policy</a>\n" +
                        "          <a href=\"#\">Terms of Service</a>\n" +
                        "          <a href=\"#\">Unsubscribe</a>\n" +
                        "          <p>© 2025 TridePay. All rights reserved.</p>\n" +
                        "        </td></tr>\n" +
                        "      </table>\n" +
                        "    </td></tr>\n" +
                        "  </table>\n" +
                        "</body>\n" +
                        "</html>";
        String htmlContent = htmlTemplate.replace("[Full Name]", fullName);
        emailService.sendHtmlEmail(email, subject, htmlContent);
    }
}
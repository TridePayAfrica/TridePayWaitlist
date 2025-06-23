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

//    @Override
//    public boolean emailExists(String email) {
//        return waitlistRepository.existsByEmail(email);
//    }



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
                        "      font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif; \n" +
                        "      -webkit-font-smoothing: antialiased;\n" +
                        "      -moz-osx-font-smoothing: grayscale;\n" +
                        "    }\n" +
                        "    .wrapper { \n" +
                        "      width: 100%; \n" +
                        "      background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);\n" +
                        "      min-height: 100vh;\n" +
                        "      padding: 60px 20px; \n" +
                        "    }\n" +
                        "    .container { \n" +
                        "      max-width: 640px; \n" +
                        "      width: 100% !important; \n" +
                        "      margin: 0 auto; \n" +
                        "      background: #ffffff;\n" +
                        "      border-radius: 24px; \n" +
                        "      overflow: hidden; \n" +
                        "      box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25), 0 0 0 1px rgba(255, 255, 255, 0.05);\n" +
                        "      backdrop-filter: blur(20px);\n" +
                        "    }\n" +
                        "    .header {\n" +
                        "      background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);\n" +
                        "      text-align: center; \n" +
                        "      padding: 50px 30px; \n" +
                        "    }\n" +
                        "    .header-content {\n" +
                        "      position: relative;\n" +
                        "      z-index: 2;\n" +
                        "      display: flex;\n" +
                        "      align-items: center;\n" +
                        "      justify-content: center;\n" +
                        "      gap: 16px;\n" +
                        "    }\n" +
                        "    .logo { width: 48px; height: 48px; display: flex; align-items: center; justify-content: center; }\n" +
                        "    .logo-card { width: 32px; height: 20px; background: #ffffff; border-radius: 6px; box-shadow: 0 4px 12px rgba(0,0,0,0.15); position: absolute; }\n" +
                        "    .header h1 { color: #ffffff; font-size: 32px; margin: 0; font-weight: 700; letter-spacing: -0.5px; text-shadow: 0 2px 10px rgba(0,0,0,0.2); }\n" +
                        "    .body { padding: 50px 40px; color: #1a1a1a; line-height: 1.7; background: linear-gradient(180deg, #ffffff 0%, #fafbff 100%); }\n" +
                        "    .body p { margin-bottom: 20px; font-size: 18px; text-align: center; color: #4a5568; max-width: 600px; margin-left: auto; margin-right: auto; }\n" +
                        "    .signature { padding: 40px; background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%); border-top: 1px solid rgba(226,232,240,0.8); }\n" +
                        "    .signature p { margin: 0; text-align: center; color: #475569; font-size: 16px; }\n" +
                        "    .footer { background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); text-align: center; padding: 40px 30px; font-size: 14px; color: #94a3b8; }\n" +
                        "    .footer a { margin: 0 8px; color: #e2e8f0; text-decoration: none; padding: 8px 16px; border-radius: 8px; transition: all 0.2s ease; font-weight: 500; }\n" +
                        "    .footer p { margin: 20px 0 0; font-size: 13px; opacity: 0.8; }\n" +
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
                        "        <tr><td class=\"header\">\n" +
                        "          <div class=\"header-content\">\n" +
                        "            <div class=\"logo\">\n" +
                        "              <div class=\"logo-card\" style=\"transform:rotate(15deg) translate(-6px,-10px);\"></div>\n" +
                        "              <div class=\"logo-card\" style=\"transform:rotate(5deg) translate(0,-3px);\"></div>\n" +
                        "              <div class=\"logo-card\" style=\"transform:rotate(-5deg) translate(6px,4px);\"></div>\n" +
                        "            </div>\n" +
                        "            <h1>TridePay</h1>\n" +
                        "          </div>\n" +
                        "        </td></tr>\n" +
                        "        <tr><td class=\"body\">\n" +
                        "          <p>Hey there! 👋 Welcome to TridePay.</p>\n" +
                        "          <p>We’re super excited to have you on the waitlist!</p>\n" +
                        "          <p>TridePay is here to help you stay on top of your money — from tracking your spending to setting budgets that actually work for you. No stress, no confusing tools. Just simple, smart money management.</p>\n" +
                        "          <p>We’ll keep you in the loop and let you know the moment we’re ready for you to jump in.</p>\n" +
                        "          <p>Thanks for joining us early — let’s make money management feel good.</p>\n" +
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
                        "</html>;";
        String htmlContent = htmlTemplate.replace("[Full Name]", fullName);
        emailService.sendHtmlEmail(email, subject, htmlContent);
    }
}
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
                        "    \n" +
                        "    .wrapper { \n" +
                        "      width: 100%; \n" +
                        "      background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #16213e 100%);\n" +
                        "      min-height: 100vh;\n" +
                        "      padding: 60px 20px; \n" +
                        "    }\n" +
                        "    \n" +
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
                        "    \n" +
                        "    .header { \n" +
                        "      background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);\n" +
                        "      text-align: center; \n" +
                        "      padding: 50px 30px; \n" +
                        "      position: relative;\n" +
                        "      overflow: hidden;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .header::before {\n" +
                        "      content: '';\n" +
                        "      position: absolute;\n" +
                        "      top: 0;\n" +
                        "      left: 0;\n" +
                        "      right: 0;\n" +
                        "      bottom: 0;\n" +
                        "      background: linear-gradient(135deg, rgba(15, 23, 42, 0.95) 0%, rgba(30, 41, 59, 0.95) 50%, rgba(51, 65, 85, 0.95) 100%);\n" +
                        "      backdrop-filter: blur(10px);\n" +
                        "    }\n" +
                        "    \n" +
                        "    .header::after {\n" +
                        "      content: '';\n" +
                        "      position: absolute;\n" +
                        "      bottom: 0;\n" +
                        "      left: 0;\n" +
                        "      right: 0;\n" +
                        "      height: 1px;\n" +
                        "      background: linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.3) 50%, transparent 100%);\n" +
                        "    }\n" +
                        "    \n" +
                        "    .header-content {\n" +
                        "      position: relative;\n" +
                        "      z-index: 2;\n" +
                        "      display: flex;\n" +
                        "      align-items: center;\n" +
                        "      justify-content: center;\n" +
                        "      gap: 16px;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .logo {\n" +
                        "      width: 48px;\n" +
                        "      height: 48px;\n" +
                        "      position: relative;\n" +
                        "      display: flex;\n" +
                        "      align-items: center;\n" +
                        "      justify-content: center;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .logo-card {\n" +
                        "      position: absolute;\n" +
                        "      width: 32px;\n" +
                        "      height: 20px;\n" +
                        "      background: #ffffff;\n" +
                        "      border-radius: 6px;\n" +
                        "      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);\n" +
                        "    }\n" +
                        "    \n" +
                        "    .logo-card:nth-child(1) {\n" +
                        "      transform: rotate(15deg) translate(-6px, -10px);\n" +
                        "      opacity: 0.9;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .logo-card:nth-child(2) {\n" +
                        "      transform: rotate(5deg) translate(0px, -3px);\n" +
                        "      opacity: 0.95;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .logo-card:nth-child(3) {\n" +
                        "      transform: rotate(-5deg) translate(6px, 4px);\n" +
                        "      opacity: 1;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .header h1 { \n" +
                        "      color: #ffffff; \n" +
                        "      font-size: 32px; \n" +
                        "      margin: 0; \n" +
                        "      font-weight: 700;\n" +
                        "      letter-spacing: -0.5px;\n" +
                        "      text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);\n" +
                        "    }\n" +
                        "    \n" +
                        "    .body { \n" +
                        "      padding: 50px 40px; \n" +
                        "      color: #1a1a1a; \n" +
                        "      line-height: 1.7; \n" +
                        "      background: linear-gradient(180deg, #ffffff 0%, #fafbff 100%);\n" +
                        "    }\n" +
                        "    \n" +
                        "    .body h2 { \n" +
                        "      margin-bottom: 30px; \n" +
                        "      font-size: 28px; \n" +
                        "      color: #1a1a2e; \n" +
                        "      font-weight: 700; \n" +
                        "      text-align: center;\n" +
                        "      letter-spacing: -0.5px;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .body p { \n" +
                        "      margin-bottom: 30px; \n" +
                        "      font-size: 18px; \n" +
                        "      text-align: center; \n" +
                        "      color: #4a5568;\n" +
                        "      max-width: 500px;\n" +
                        "      margin-left: auto;\n" +
                        "      margin-right: auto;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .features-grid { \n" +
                        "      display: grid; \n" +
                        "      grid-template-columns: 1fr 1fr; \n" +
                        "      gap: 24px; \n" +
                        "      margin: 40px 0; \n" +
                        "    }\n" +
                        "    \n" +
                        "    .card { \n" +
                        "      background: #ffffff;\n" +
                        "      border-radius: 16px; \n" +
                        "      padding: 28px;\n" +
                        "      position: relative;\n" +
                        "      border: 1px solid rgba(30, 58, 138, 0.1);\n" +
                        "      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);\n" +
                        "      overflow: hidden;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .card::before {\n" +
                        "      content: '';\n" +
                        "      position: absolute;\n" +
                        "      top: 0;\n" +
                        "      left: 0;\n" +
                        "      right: 0;\n" +
                        "      height: 3px;\n" +
                        "      background: linear-gradient(90deg, #1e3a8a 0%, #3b82f6 100%);\n" +
                        "      transform: translateX(-100%);\n" +
                        "      transition: transform 0.3s ease;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .card:hover::before {\n" +
                        "      transform: translateX(0);\n" +
                        "    }\n" +
                        "    \n" +
                        "    .card:hover { \n" +
                        "      transform: translateY(-8px); \n" +
                        "      box-shadow: 0 20px 40px rgba(30, 58, 138, 0.15);\n" +
                        "      border-color: rgba(30, 58, 138, 0.2);\n" +
                        "    }\n" +
                        "    \n" +
                        "    .card-content h3 { \n" +
                        "      margin: 0 0 12px; \n" +
                        "      font-size: 20px; \n" +
                        "      color: #1a1a2e; \n" +
                        "      font-weight: 600;\n" +
                        "      letter-spacing: -0.3px;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .card-content p { \n" +
                        "      margin: 0; \n" +
                        "      font-size: 15px; \n" +
                        "      color: #64748b; \n" +
                        "      line-height: 1.6;\n" +
                        "      text-align: left;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .signature { \n" +
                        "      padding: 40px; \n" +
                        "      background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);\n" +
                        "      border-top: 1px solid rgba(226, 232, 240, 0.8);\n" +
                        "    }\n" +
                        "    \n" +
                        "    .signature p { \n" +
                        "      margin-bottom: 24px; \n" +
                        "      text-align: center; \n" +
                        "      color: #475569;\n" +
                        "      font-size: 16px;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .signature .name { \n" +
                        "      font-weight: 700; \n" +
                        "      color: #1a1a2e; \n" +
                        "      margin-bottom: 8px; \n" +
                        "      font-size: 20px; \n" +
                        "      text-align: center;\n" +
                        "      letter-spacing: -0.3px;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .signature .title { \n" +
                        "      font-size: 15px; \n" +
                        "      color: #64748b; \n" +
                        "      text-align: center; \n" +
                        "    }\n" +
                        "    \n" +
                        "    .footer { \n" +
                        "      background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);\n" +
                        "      text-align: center; \n" +
                        "      padding: 40px 30px; \n" +
                        "      font-size: 14px; \n" +
                        "      color: #94a3b8; \n" +
                        "    }\n" +
                        "    \n" +
                        "    .footer-links {\n" +
                        "      margin-bottom: 20px;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .footer a { \n" +
                        "      margin: 0 8px; \n" +
                        "      color: #e2e8f0; \n" +
                        "      text-decoration: none; \n" +
                        "      padding: 8px 16px; \n" +
                        "      border-radius: 8px; \n" +
                        "      transition: all 0.2s ease;\n" +
                        "      font-weight: 500;\n" +
                        "    }\n" +
                        "    \n" +
                        "    .footer a:hover { \n" +
                        "      background: rgba(30, 58, 138, 0.2);\n" +
                        "      color: #ffffff;\n" +
                        "      transform: translateY(-1px);\n" +
                        "    }\n" +
                        "    \n" +
                        "    .footer p { \n" +
                        "      margin: 0; \n" +
                        "      font-size: 13px;\n" +
                        "      opacity: 0.8;\n" +
                        "    }\n" +
                        "    \n" +
                        "    @media only screen and (max-width: 480px) {\n" +
                        "      .wrapper { \n" +
                        "        padding: 20px 10px !important; \n" +
                        "      }\n" +
                        "      \n" +
                        "      .body, .signature { \n" +
                        "        padding: 30px 25px !important; \n" +
                        "      }\n" +
                        "      \n" +
                        "      .header { \n" +
                        "        padding: 40px 20px !important; \n" +
                        "      }\n" +
                        "      \n" +
                        "      .header-content {\n" +
                        "        flex-direction: column;\n" +
                        "        gap: 12px;\n" +
                        "      }\n" +
                        "      \n" +
                        "      .header h1 { \n" +
                        "        font-size: 26px !important; \n" +
                        "      }\n" +
                        "      \n" +
                        "      .body h2 { \n" +
                        "        font-size: 24px !important; \n" +
                        "      }\n" +
                        "      \n" +
                        "      .body p {\n" +
                        "        font-size: 16px !important;\n" +
                        "      }\n" +
                        "      \n" +
                        "      .features-grid { \n" +
                        "        grid-template-columns: 1fr !important;\n" +
                        "        gap: 16px;\n" +
                        "      }\n" +
                        "      \n" +
                        "      .card { \n" +
                        "        padding: 20px !important; \n" +
                        "      }\n" +
                        "      \n" +
                        "      .footer { \n" +
                        "        padding: 30px 20px !important; \n" +
                        "      }\n" +
                        "      \n" +
                        "      .footer a { \n" +
                        "        display: inline-block; \n" +
                        "        margin: 4px 6px !important; \n" +
                        "      }\n" +
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
                        "              <div class=\"logo-card\"></div>\n" +
                        "              <div class=\"logo-card\"></div>\n" +
                        "              <div class=\"logo-card\"></div>\n" +
                        "            </div>\n" +
                        "            <h1>TridePay</h1>\n" +
                        "          </div>\n" +
                        "        </td></tr>\n" +
                        "        <tr><td class=\"body\">\n" +
                        "          <h2>Welcome aboard, [Full Name]</h2>\n" +
                        "          <p>Thank you for joining our waitlist. You're now part of the select group helping us build Nigeria's most professional payments platform.</p>\n" +
                        "          <div class=\"features-grid\">\n" +
                        "            <div class=\"card\">\n" +
                        "              <div class=\"card-content\">\n" +
                        "                <h3>Secure Digital Wallet</h3>\n" +
                        "                <p>Store all your cards in one encrypted vault.</p>\n" +
                        "              </div>\n" +
                        "            </div>\n" +
                        "            <div class=\"card\">\n" +
                        "              <div class=\"card-content\">\n" +
                        "                <h3>Instant Tap Payments</h3>\n" +
                        "                <p>Fast, reliable NFC transactions anywhere.</p>\n" +
                        "              </div>\n" +
                        "            </div>\n" +
                        "            <div class=\"card\">\n" +
                        "              <div class=\"card-content\">\n" +
                        "                <h3>Virtual Dollar Cards</h3>\n" +
                        "                <p>Create and manage multi-currency cards instantly.</p>\n" +
                        "              </div>\n" +
                        "            </div>\n" +
                        "            <div class=\"card\">\n" +
                        "              <div class=\"card-content\">\n" +
                        "                <h3>Payment Splitting</h3>\n" +
                        "                <p>Easily divide bills and split costs among people.</p>\n" +
                        "              </div>\n" +
                        "            </div>\n" +
                        "          </div>\n" +
                        "        </td></tr>\n" +
                        "        <tr><td class=\"signature\">\n" +
                        "          <p>We're excited to have you with us. Expect exclusive updates as we move closer to launch.</p>\n" +
                        "          <p class=\"name\">Oluwakamiye Adetula</p>\n" +
                        "          <p class=\"title\">CEO &amp; Co-Founder, TridePay</p>\n" +
                        "        </td></tr>\n" +
                        "        <tr><td class=\"footer\">\n" +
                        "          <div class=\"footer-links\">\n" +
                        "            <a href=\"#\">Privacy Policy</a>\n" +
                        "            <a href=\"#\">Terms of Service</a>\n" +
                        "            <a href=\"#\">Unsubscribe</a>\n" +
                        "          </div>\n" +
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
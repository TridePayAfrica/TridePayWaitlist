package com.tride.tridewaitlist.service;

import com.tride.tridewaitlist.model.Waitlist;
import com.tride.tridewaitlist.repository.WaitlistRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class WaitlistServiceImpl implements WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final EmailService emailService;

    public WaitlistServiceImpl(WaitlistRepository waitlistRepository, EmailService emailService) {
        this.waitlistRepository = waitlistRepository;
        this.emailService = emailService;
    }

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    @Override
    public boolean emailExists(String email) {
        return waitlistRepository.existsByEmail(email);
    }

    @Override
    public void addToWaitlist(Waitlist waitlist) {
        if (!isValidEmail(waitlist.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        waitlist.setJoinDate(LocalDateTime.now());
        waitlistRepository.save(waitlist);
        sendWaitlistConfirmationEmail(waitlist.getEmail(), waitlist.getFullName());
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_REGEX);
    }

    private void sendWaitlistConfirmationEmail(String email, String fullName) {
        String subject = "Welcome to Our Waitlist!";
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>You're on the Waitlist!</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: 'Arial', sans-serif; line-height: 1.6; color: #071623; background-color: #f4f4f4; padding: 20px;\">\n" +
                "    <div class=\"email-container\" style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\">\n" +
                "        <header class=\"email-header\" style=\"background-color: #071623; color: #ffffff; padding: 20px; text-align: center;\">\n" +
                "            <div class=\"logo\" style=\"display: flex; align-items: center; justify-content: center;\">\n" +
                "                <h1 style=\"font-size: 32px; font-weight: bold; margin: 0; letter-spacing: 1px;\">TridePay</h1>\n" +
                "            </div>\n" +
                "        </header>\n" +
                "        \n" +
                "        <main class=\"email-body\" style=\"padding: 30px; background-color: #ffffff;\">\n" +
                "            <div class=\"welcome-section\" style=\"margin-bottom: 25px;\">\n" +
                "                <h2 style=\"color: #2D5679; margin-bottom: 15px; font-size: 24px;\">Welcome to TridePay!</h2>\n" +
                "                <p class=\"greeting\" style=\"font-weight: bold; margin-bottom: 10px;\">Dear {{fullName}},</p>\n" +
                "                <p>We're thrilled to have you on board as we build the future of seamless payments.</p>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"features-section\" style=\"background-color: #DFEAFA; padding: 20px; border-radius: 8px; margin-bottom: 25px;\">\n" +
                "                <p class=\"intro\" style=\"margin-bottom: 15px;\">TridePay is designed to make managing your cards and transactions easier than ever. With our innovative features:</p>\n" +
                "                <ul class=\"features-list\" style=\"list-style-type: none; margin-left: 10px; margin-bottom: 15px;\">\n" +
                "                    <li style=\"margin-bottom: 10px; display: flex; align-items: center;\"><span class=\"feature-icon\" style=\"margin-right: 10px; font-size: 18px;\">\uD83D\uDCB3</span> Store all your ATM cards in one place</li>\n" +
                "                    <li style=\"margin-bottom: 10px; display: flex; align-items: center;\"><span class=\"feature-icon\" style=\"margin-right: 10px; font-size: 18px;\">\uD83D\uDCF1</span> Make effortless NFC payments</li>\n" +
                "                    <li style=\"margin-bottom: 10px; display: flex; align-items: center;\"><span class=\"feature-icon\" style=\"margin-right: 10px; font-size: 18px;\">\uD83D\uDCB0</span> Create virtual dollar cards</li>\n" +
                "                    <li style=\"margin-bottom: 10px; display: flex; align-items: center;\"><span class=\"feature-icon\" style=\"margin-right: 10px; font-size: 18px;\">\uD83D\uDCDC</span> Easily manage mobile and service provider payments</li>\n" +
                "                </ul>\n" +
                "                <p class=\"outro\" style=\"font-weight: bold; color: #2D5679;\">We're redefining convenience for Nigerians.</p>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"waitlist-section\" style=\"margin-bottom: 25px; padding: 15px; border-left: 4px solid #58A1DD;\">\n" +
                "                <p>As an early member of our waitlist, you'll be among the first to experience TridePay when we launch. Stay tuned for:</p>\n" +
                "                <ul class=\"waitlist-benefits\" style=\"margin-left: 25px; margin-top: 10px; margin-bottom: 10px;\">\n" +
                "                    <li style=\"margin-bottom: 5px;\">Exclusive updates</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\">Beta access</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\">Special offers</li>\n" +
                "                </ul>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"closing-section\" style=\"margin-bottom: 20px;\">\n" +
                "                <p>Thank you for joining us on this journey. If you have any questions, feel free to reply to this email—we'd love to hear from you.</p>\n" +
                "                <p>Looking forward to making payments effortless with you.</p>\n" +
                "                <p class=\"signature\" style=\"margin-top: 20px; font-weight: bold;\">Best regards,</p>\n" +
                "                <p class=\"name\" style=\"font-weight: bold; color: #2D5679;\">Oluwakamiye Adetula</p>\n" +
                "                <p class=\"title\" style=\"color: #427AAA; font-style: italic;\">CEO & Co-founder, TridePay</p>\n" +
                "            </div>\n" +
                "        </main>\n" +
                "        \n" +
                "        <footer class=\"email-footer\" style=\"background-color: #19354C; color: #ffffff; padding: 20px; text-align: center;\">\n" +
                "            <div class=\"social-links\" style=\"margin-bottom: 15px;\">\n" +
                "                <a href=\"#\" class=\"social-icon\" style=\"color: #9EC5F0; text-decoration: none; margin: 0 10px; font-size: 14px;\">Twitter</a>\n" +
                "                <a href=\"#\" class=\"social-icon\" style=\"color: #9EC5F0; text-decoration: none; margin: 0 10px; font-size: 14px;\">Facebook</a>\n" +
                "                <a href=\"#\" class=\"social-icon\" style=\"color: #9EC5F0; text-decoration: none; margin: 0 10px; font-size: 14px;\">Instagram</a>\n" +
                "                <a href=\"#\" class=\"social-icon\" style=\"color: #9EC5F0; text-decoration: none; margin: 0 10px; font-size: 14px;\">LinkedIn</a>\n" +
                "            </div>\n" +
                "            <div class=\"footer-info\" style=\"font-size: 12px; color: #9EC5F0;\">\n" +
                "                <p>&copy; 2025 TridePay. All rights reserved.</p>\n" +
                "                <p>\n" +
                "                    <a href=\"#\" style=\"color: #9EC5F0; text-decoration: none;\">Privacy Policy</a> | \n" +
                "                    <a href=\"#\" style=\"color: #9EC5F0; text-decoration: none;\">Terms of Service</a> | \n" +
                "                    <a href=\"#\" style=\"color: #9EC5F0; text-decoration: none;\">Unsubscribe</a>\n" +
                "                </p>\n" +
                "            </div>\n" +
                "        </footer>\n" +
                "    </div>\n" +
                "\n" +
                "    <style type=\"text/css\">\n" +
                "        @media screen and (max-width: 480px) {\n" +
                "            .link {\n" +
                "                margin-left: 5px;\n" +
                "            } \n" +
                "            .main-heading {\n" +
                "                color: #29003d;\n" +
                "                font-family: Noto Sans, sans-serif;\n" +
                "                font-size: 20px;\n" +
                "                font-style: normal;\n" +
                "                font-weight: 500;\n" +
                "                line-height: normal;\n" +
                "                width: 250px;\n" +
                "                margin: 0;\n" +
                "                margin-top: 44px;\n" +
                "            }\n" +
                "        }\n" +
                "        @media screen and (max-width: 768px) {\n" +
                "            .footer-container {\n" +
                "                padding: 48px 12px;\n" +
                "            }\n" +
                "            .footer-logo {\n" +
                "                margin: 0;\n" +
                "                margin-right: 25%;\n" +
                "                font-size: 16px;\n" +
                "            }\n" +
                "            .footer-social-icon {\n" +
                "                width: 24px;\n" +
                "                height: 24px;\n" +
                "                margin-right: 10px;\n" +
                "            }\n" +
                "            .footer-address {\n" +
                "                text-align: left;\n" +
                "                margin: 0;\n" +
                "                font-size: 14px;\n" +
                "            }\n" +
                "            .footer-address-div {\n" +
                "                display: block;\n" +
                "            }\n" +
                "            .link {\n" +
                "                margin-left: 20%;\n" +
                "            }\n" +
                "        }\n" +
                "        @media screen and (min-width: 768px) {\n" +
                "            .footer-container {\n" +
                "                padding: 48px 48px;\n" +
                "                margin-top: 67px;\n" +
                "            }\n" +
                "            .footer-logo {\n" +
                "                margin: 0;\n" +
                "                margin-right: 55%;\n" +
                "                font-size: 20px;\n" +
                "            }\n" +
                "            .footer-social-icon {\n" +
                "                width: 24px;\n" +
                "                height: 24px;\n" +
                "                margin-right: 20px;\n" +
                "            }\n" +
                "            .footer-address-div {\n" +
                "                display: flex;\n" +
                "                align-items: flex-start;\n" +
                "            }\n" +
                "            .footer-address {\n" +
                "                text-align: right;\n" +
                "                margin: 0;\n" +
                "                font-size: 16px;\n" +
                "            }\n" +
                "            .main-heading {\n" +
                "                color: #29003d;\n" +
                "                font-family: Noto Sans, sans-serif;\n" +
                "                font-size: 28px;\n" +
                "                font-style: normal;\n" +
                "                font-weight: 500;\n" +
                "                line-height: normal;\n" +
                "                width: 300px;\n" +
                "                max-width: 50%;\n" +
                "                margin: 0;\n" +
                "                margin-top: 44px;\n" +
                "            }\n" +
                "            .link {\n" +
                "                margin-left: 60%;\n" +
                "            }\n" +
                "        }\n" +
                "    </style>\n" +
                "</body>\n" +
                "</html>\n" +
                "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>You're on the Waitlist!</title>\n" +
                "</head>\n" +
                "<body style=\"font-family: 'Arial', sans-serif; line-height: 1.6; color: #071623; background-color: #f4f4f4; padding: 20px;\">\n" +
                "    <div class=\"email-container\" style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\">\n" +
                "        <header class=\"email-header\" style=\"background-color: #071623; color: #ffffff; padding: 20px; text-align: center;\">\n" +
                "            <div class=\"logo\" style=\"display: flex; align-items: center; justify-content: center;\">\n" +
                "                <h1 style=\"font-size: 32px; font-weight: bold; margin: 0; letter-spacing: 1px;\">TridePay</h1>\n" +
                "            </div>\n" +
                "        </header>\n" +
                "        \n" +
                "        <main class=\"email-body\" style=\"padding: 30px; background-color: #ffffff;\">\n" +
                "            <div class=\"welcome-section\" style=\"margin-bottom: 25px;\">\n" +
                "                <h2 style=\"color: #2D5679; margin-bottom: 15px; font-size: 24px;\">Welcome to TridePay!</h2>\n" +
                "                <p class=\"greeting\" style=\"font-weight: bold; margin-bottom: 10px;\">Dear " + fullName + ",</p>\n" +
                "                <p>We're thrilled to have you on board as we build the future of seamless payments.</p>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"features-section\" style=\"background-color: #DFEAFA; padding: 20px; border-radius: 8px; margin-bottom: 25px;\">\n" +
                "                <p class=\"intro\" style=\"margin-bottom: 15px;\">TridePay is designed to make managing your cards and transactions easier than ever. With our innovative features:</p>\n" +
                "                <ul class=\"features-list\" style=\"list-style-type: none; margin-left: 10px; margin-bottom: 15px;\">\n" +
                "                    <li style=\"margin-bottom: 10px; display: flex; align-items: center;\"><span class=\"feature-icon\" style=\"margin-right: 10px; font-size: 18px;\">\uD83D\uDCB3</span> Store all your ATM cards in one place</li>\n" +
                "                    <li style=\"margin-bottom: 10px; display: flex; align-items: center;\"><span class=\"feature-icon\" style=\"margin-right: 10px; font-size: 18px;\">\uD83D\uDCF1</span> Make effortless NFC payments</li>\n" +
                "                    <li style=\"margin-bottom: 10px; display: flex; align-items: center;\"><span class=\"feature-icon\" style=\"margin-right: 10px; font-size: 18px;\">\uD83D\uDCB0</span> Create virtual dollar cards</li>\n" +
                "                    <li style=\"margin-bottom: 10px; display: flex; align-items: center;\"><span class=\"feature-icon\" style=\"margin-right: 10px; font-size: 18px;\">\uD83D\uDCDC</span> Easily manage mobile and service provider payments</li>\n" +
                "                </ul>\n" +
                "                <p class=\"outro\" style=\"font-weight: bold; color: #2D5679;\">We're redefining convenience for Nigerians.</p>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"waitlist-section\" style=\"margin-bottom: 25px; padding: 15px; border-left: 4px solid #58A1DD;\">\n" +
                "                <p>As an early member of our waitlist, you'll be among the first to experience TridePay when we launch. Stay tuned for:</p>\n" +
                "                <ul class=\"waitlist-benefits\" style=\"margin-left: 25px; margin-top: 10px; margin-bottom: 10px;\">\n" +
                "                    <li style=\"margin-bottom: 5px;\">Exclusive updates</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\">Beta access</li>\n" +
                "                    <li style=\"margin-bottom: 5px;\">Special offers</li>\n" +
                "                </ul>\n" +
                "            </div>\n" +
                "            \n" +
                "            <div class=\"closing-section\" style=\"margin-bottom: 20px;\">\n" +
                "                <p>Thank you for joining us on this journey. If you have any questions, feel free to reply to this email—we'd love to hear from you.</p>\n" +
                "                <p>Looking forward to making payments effortless with you.</p>\n" +
                "                <p class=\"signature\" style=\"margin-top: 20px; font-weight: bold;\">Best regards,</p>\n" +
                "                <p class=\"name\" style=\"font-weight: bold; color: #2D5679;\">Oluwakamiye Adetula</p>\n" +
                "                <p class=\"title\" style=\"color: #427AAA; font-style: italic;\">CEO & Co-founder, TridePay</p>\n" +
                "            </div>\n" +
                "        </main>\n" +
                "        \n" +
                "        <footer class=\"email-footer\" style=\"background-color: #19354C; color: #ffffff; padding: 20px; text-align: center;\">\n" +
                "            <div class=\"social-links\" style=\"margin-bottom: 15px;\">\n" +
                "                <a href=\"#\" class=\"social-icon\" style=\"color: #9EC5F0; text-decoration: none; margin: 0 10px; font-size: 14px;\">Twitter</a>\n" +
                "                <a href=\"#\" class=\"social-icon\" style=\"color: #9EC5F0; text-decoration: none; margin: 0 10px; font-size: 14px;\">Facebook</a>\n" +
                "                <a href=\"#\" class=\"social-icon\" style=\"color: #9EC5F0; text-decoration: none; margin: 0 10px; font-size: 14px;\">Instagram</a>\n" +
                "                <a href=\"#\" class=\"social-icon\" style=\"color: #9EC5F0; text-decoration: none; margin: 0 10px; font-size: 14px;\">LinkedIn</a>\n" +
                "            </div>\n" +
                "            <div class=\"footer-info\" style=\"font-size: 12px; color: #9EC5F0;\">\n" +
                "                <p>&copy; 2025 TridePay. All rights reserved.</p>\n" +
                "                <p>\n" +
                "                    <a href=\"#\" style=\"color: #9EC5F0; text-decoration: none;\">Privacy Policy</a> | \n" +
                "                    <a href=\"#\" style=\"color: #9EC5F0; text-decoration: none;\">Terms of Service</a> | \n" +
                "                    <a href=\"#\" style=\"color: #9EC5F0; text-decoration: none;\">Unsubscribe</a>\n" +
                "                </p>\n" +
                "            </div>\n" +
                "        </footer>\n" +
                "    </div>\n" +
                "\n" +
                "    <style type=\"text/css\">\n" +
                "        @media screen and (max-width: 480px) {\n" +
                "            .link {\n" +
                "                margin-left: 5px;\n" +
                "            } \n" +
                "            .main-heading {\n" +
                "                color: #29003d;\n" +
                "                font-family: Noto Sans, sans-serif;\n" +
                "                font-size: 20px;\n" +
                "                font-style: normal;\n" +
                "                font-weight: 500;\n" +
                "                line-height: normal;\n" +
                "                width: 250px;\n" +
                "                margin: 0;\n" +
                "                margin-top: 44px;\n" +
                "            }\n" +
                "        }\n" +
                "        @media screen and (max-width: 768px) {\n" +
                "            .footer-container {\n" +
                "                padding: 48px 12px;\n" +
                "            }\n" +
                "            .footer-logo {\n" +
                "                margin: 0;\n" +
                "                margin-right: 25%;\n" +
                "                font-size: 16px;\n" +
                "            }\n" +
                "            .footer-social-icon {\n" +
                "                width: 24px;\n" +
                "                height: 24px;\n" +
                "                margin-right: 10px;\n" +
                "            }\n" +
                "            .footer-address {\n" +
                "                text-align: left;\n" +
                "                margin: 0;\n" +
                "                font-size: 14px;\n" +
                "            }\n" +
                "            .footer-address-div {\n" +
                "                display: block;\n" +
                "            }\n" +
                "            .link {\n" +
                "                margin-left: 20%;\n" +
                "            }\n" +
                "        }\n" +
                "        @media screen and (min-width: 768px) {\n" +
                "            .footer-container {\n" +
                "                padding: 48px 48px;\n" +
                "                margin-top: 67px;\n" +
                "            }\n" +
                "            .footer-logo {\n" +
                "                margin: 0;\n" +
                "                margin-right: 55%;\n" +
                "                font-size: 20px;\n" +
                "            }\n" +
                "            .footer-social-icon {\n" +
                "                width: 24px;\n" +
                "                height: 24px;\n" +
                "                margin-right: 20px;\n" +
                "            }\n" +
                "            .footer-address-div {\n" +
                "                display: flex;\n" +
                "                align-items: flex-start;\n" +
                "            }\n" +
                "            .footer-address {\n" +
                "                text-align: right;\n" +
                "                margin: 0;\n" +
                "                font-size: 16px;\n" +
                "            }\n" +
                "            .main-heading {\n" +
                "                color: #29003d;\n" +
                "                font-family: Noto Sans, sans-serif;\n" +
                "                font-size: 28px;\n" +
                "                font-style: normal;\n" +
                "                font-weight: 500;\n" +
                "                line-height: normal;\n" +
                "                width: 300px;\n" +
                "                max-width: 50%;\n" +
                "                margin: 0;\n" +
                "                margin-top: 44px;\n" +
                "            }\n" +
                "            .link {\n" +
                "                margin-left: 60%;\n" +
                "            }\n" +
                "        }\n" +
                "    </style>\n" +
                "</body>\n" +
                "</html>\n";

        try {
            emailService.sendHtmlEmail(email, subject, htmlContent);
        } catch (MessagingException e) {
           System.out.println();
        }
    }
}
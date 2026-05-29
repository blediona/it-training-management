package services;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {
    private static final String DEFAULT_PORT = "587";

    public void sendPasswordResetCode(String recipientEmail, String token) {
        String appName = I18nService.get("app.title");
        String subject = I18nService.get("auth.resetEmailSubject");
        String body = String.format(I18nService.get("auth.resetEmailBody"), appName, token);
        send(recipientEmail, subject, body);
    }

    private void send(String recipientEmail, String subject, String body) {
        String host = getConfig("smtp.host", "SMTP_HOST", "");
        String port = getConfig("smtp.port", "SMTP_PORT", DEFAULT_PORT);
        String username = getConfig("smtp.user", "SMTP_USER", "");
        String password = getConfig("smtp.password", "SMTP_PASSWORD", "");
        String from = getConfig("smtp.from", "SMTP_FROM", username);

        if (host.isBlank() || username.isBlank() || password.isBlank() || from.isBlank()) {
            throw new IllegalStateException(I18nService.get("auth.emailConfigMissing"));
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException(I18nService.get("auth.emailSendFailed") + " " + e.getMessage(), e);
        }
    }

    private String getConfig(String propertyKey, String environmentKey, String defaultValue) {
        String propertyValue = System.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String environmentValue = System.getenv(environmentKey);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }

        return defaultValue;
    }
}

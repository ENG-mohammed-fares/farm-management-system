package com.smartfarm.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

    private static final String ENV_FROM = "SMARTFARM_EMAIL_FROM";
    private static final String ENV_PASSWORD = "SMARTFARM_EMAIL_APP_PASSWORD";

    private static String cachedFrom;
    private static String cachedPassword;
    private static String lastError = "";
    private static boolean loaded;

    public static String getLastError() {
        return lastError;
    }

    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }

        cachedFrom = firstNonBlank(System.getenv(ENV_FROM), null);
        cachedPassword = firstNonBlank(System.getenv(ENV_PASSWORD), null);

        if (cachedFrom == null || cachedPassword == null) {
            Properties fileProps = loadLocalProperties();
            if (cachedFrom == null) {
                cachedFrom = firstNonBlank(fileProps.getProperty("email.from"), null);
            }
            if (cachedPassword == null) {
                cachedPassword = firstNonBlank(fileProps.getProperty("email.appPassword"), null);
            }
        }

        // Only cache a successful load so a later call can retry after the config file appears.
        if (cachedFrom != null && cachedPassword != null) {
            loaded = true;
        }
    }

    /** Clears cache so credentials can be reloaded after editing the config file. */
    public static synchronized void reloadCredentials() {
        loaded = false;
        cachedFrom = null;
        cachedPassword = null;
        lastError = "";
    }

    private static String firstNonBlank(String value, String fallback) {
        if (value != null && !value.isBlank()) {
            return value.trim();
        }
        return fallback;
    }

    private static Properties loadLocalProperties() {
        Properties props = new Properties();
        String userDir = System.getProperty("user.dir", ".");
        String userHome = System.getProperty("user.home", ".");

        Path[] candidates = {
                Path.of(userDir, "config", "email.properties"),
                Path.of(userDir, "farm_managment_system", "config", "email.properties"),
                Path.of(userDir, "email.properties"),
                Path.of(userHome, ".smartfarm", "email.properties"),
                Path.of("config", "email.properties"),
                Path.of("email.properties")
        };

        for (Path path : candidates) {
            if (Files.isRegularFile(path)) {
                try (InputStream in = Files.newInputStream(path)) {
                    props.load(in);
                    System.out.println("EmailService: loaded credentials from " + path.toAbsolutePath());
                    return props;
                } catch (IOException e) {
                    System.err.println("EmailService: failed reading " + path + ": " + e.getMessage());
                }
            }
        }

        try (InputStream in = EmailService.class.getResourceAsStream("/config/email.properties")) {
            if (in != null) {
                props.load(in);
                System.out.println("EmailService: loaded credentials from classpath /config/email.properties");
            }
        } catch (IOException e) {
            System.err.println("EmailService: failed reading classpath config: " + e.getMessage());
        }

        return props;
    }

    private static boolean isConfigured() {
        ensureLoaded();
        return cachedFrom != null && !cachedFrom.isBlank()
                && cachedPassword != null && !cachedPassword.isBlank();
    }

    public static boolean sendVerificationCode(String toEmail, String code) {
        lastError = "";

        if (!isConfigured()) {
            lastError = "Email not configured. Create config/email.properties or set "
                    + ENV_FROM + " / " + ENV_PASSWORD;
            System.err.println("EmailService: " + lastError);
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        final String from = cachedFrom;
        final String password = cachedPassword;

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "Smart Farm System"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Password Reset - Smart Farm");

            String html = "<div style='font-family:Arial;max-width:400px;margin:auto;padding:30px;"
                    + "border:1px solid #e0e0e0;border-radius:12px;'>"
                    + "<h2 style='color:#2E7D32;text-align:center;'>Smart Farm</h2>"
                    + "<p style='color:#555;text-align:center;'>Your verification code is:</p>"
                    + "<div style='background:#E8F5E9;border-radius:8px;padding:15px;"
                    + "text-align:center;margin:20px 0;'>"
                    + "<span style='font-size:32px;font-weight:bold;color:#2E7D32;"
                    + "letter-spacing:8px;'>" + code + "</span></div>"
                    + "<p style='color:#888;font-size:12px;text-align:center;'>"
                    + "This code expires in 5 minutes.</p></div>";

            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
            return true;

        } catch (AuthenticationFailedException e) {
            lastError = "Gmail login failed. Check App Password in config/email.properties";
            e.printStackTrace();
            return false;
        } catch (MessagingException e) {
            lastError = "Could not send email: " + e.getMessage();
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            lastError = "Email error: " + e.getMessage();
            e.printStackTrace();
            return false;
        }
    }
}

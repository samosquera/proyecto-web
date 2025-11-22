package com.bers.security.msg;

import com.bers.security.msg.NotificationDtos.EmailNotification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * Enviar correo con plantilla HTML
     */
    public void sendEmail(EmailNotification notification) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getTo());
            helper.setSubject(notification.getSubject());
            helper.setFrom("BersApp <noreply@busconnect.com>");

            // Procesar plantilla Thymeleaf
            String htmlContent = processTemplate(
                    notification.getTemplateName(),
                    notification.getVariables()
            );
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", notification.getTo());

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", notification.getTo(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Procesar plantilla Thymeleaf
     */
    private String processTemplate(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }
        return templateEngine.process(templateName, context);
    }

    // ==================== MÉTODOS DE CONVENIENCIA ====================

    /**
     * Enviar correo de bienvenida
     */
    public void sendWelcomeEmail(String to, String username) {
        EmailNotification notification = EmailNotification.builder()
                .to(to)
                .subject("¡Bienvenido a BusConnect!")
                .templateName("welcome-email")
                .variables(Map.of(
                        "username", username,
                        "year", String.valueOf(java.time.Year.now().getValue())
                ))
                .build();

        sendEmail(notification);
    }

    /**
     * Enviar código de verificación
     */
    public void sendVerificationCode(String to, String username, String code) {
        EmailNotification notification = EmailNotification.builder()
                .to(to)
                .subject("Código de verificación - BusConnect")
                .templateName("verification-code")
                .variables(Map.of(
                        "username", username,
                        "code", code,
                        "expirationMinutes", "10"
                ))
                .build();

        sendEmail(notification);
    }

    /**
     * Enviar confirmación de ticket
     */
    public void sendTicketConfirmation(String to, String username, Map<String, Object> ticketData) {
        EmailNotification notification = EmailNotification.builder()
                .to(to)
                .subject("Confirmación de Ticket - BusConnect")
                .templateName("ticket-confirmation")
                .variables(Map.of(
                        "username", username,
                        "ticketData", ticketData
                ))
                .build();

        sendEmail(notification);
    }

    /**
     * Enviar notificación de cambio de andén
     */
    public void sendPlatformChange(String to, String username, String oldPlatform, String newPlatform) {
        EmailNotification notification = EmailNotification.builder()
                .to(to)
                .subject("⚠️ Cambio de Andén - BusConnect")
                .templateName("platform-change")
                .variables(Map.of(
                        "username", username,
                        "oldPlatform", oldPlatform,
                        "newPlatform", newPlatform
                ))
                .build();

        sendEmail(notification);
    }

    /**
     * Enviar recordatorio de viaje
     */
    public void sendTripReminder(String to, String username, Map<String, Object> tripData) {
        EmailNotification notification = EmailNotification.builder()
                .to(to)
                .subject("Recordatorio de Viaje - BusConnect")
                .templateName("trip-reminder")
                .variables(Map.of(
                        "username", username,
                        "tripData", tripData
                ))
                .build();

        sendEmail(notification);
    }

    /**
     * Enviar notificación de cambio de contraseña
     */
    public void sendPasswordChangedNotification(String to, String username) {
        EmailNotification notification = EmailNotification.builder()
                .to(to)
                .subject("Contraseña Cambiada - BusConnect")
                .templateName("password-changed")
                .variables(Map.of(
                        "username", username,
                        "changeDate", java.time.LocalDateTime.now().toString()
                ))
                .build();

        sendEmail(notification);
    }

    /**
     * Enviar link de recuperación de contraseña
     */
    public void sendPasswordResetEmail(String to, String username, String resetLink) {
        EmailNotification notification = EmailNotification.builder()
                .to(to)
                .subject("Recuperación de Contraseña - BersApp")
                .templateName("password-reset")
                .variables(Map.of(
                        "username", username,
                        "resetLink", resetLink,
                        "expirationHours", "1"
                ))
                .build();

        sendEmail(notification);
    }
}
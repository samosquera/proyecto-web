package com.bers.security.msg;

import com.bers.security.msg.NotificationDtos.EmailNotification;
import com.bers.security.msg.NotificationDtos.SmsNotification;
import com.bers.security.msg.NotificationDtos.WhatsAppNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    // ==================== EMAIL NOTIFICATIONS ====================

    public void sendEmailNotification(EmailNotification notification) {
        try {
            rabbitTemplate.convertAndSend(
                    NotificationConfig.NOTIFICATION_EXCHANGE,
                    NotificationConfig.EMAIL_ROUTING_KEY,
                    notification
            );
            log.info("Email notification queued for: {}", notification.getTo());
        } catch (Exception e) {
            log.error("Failed to queue email notification", e);
            throw new RuntimeException("Failed to queue email notification", e);
        }
    }

    public void sendWelcomeEmail(String to, String username) {
        EmailNotification notification = EmailNotification.builder()
                .to(to)
                .subject("¡Bienvenido a BusConnect!")
                .templateName("welcome-email")
                .variables(Map.of("username", username))
                .build();

        sendEmailNotification(notification);
    }

    public void sendVerificationEmail(String to, String username, String code) {
        EmailNotification notification = EmailNotification.builder()
                .to(to)
                .subject("Código de Verificación")
                .templateName("verification-code")
                .variables(Map.of(
                        "username", username,
                        "code", code,
                        "expirationMinutes", "10"
                ))
                .build();

        sendEmailNotification(notification);
    }

    public void sendTicketConfirmation(String to, String username, Map<String, Object> ticketData) {
        EmailNotification notification = EmailNotification.builder()
                .to(to)
                .subject("Confirmación de Ticket")
                .templateName("ticket-confirmation")
                .variables(Map.of(
                        "username", username,
                        "ticketData", ticketData
                ))
                .build();

        sendEmailNotification(notification);
    }

    // ==================== SMS NOTIFICATIONS ====================

    public void sendSmsNotification(SmsNotification notification) {
        try {
            rabbitTemplate.convertAndSend(
                    NotificationConfig.NOTIFICATION_EXCHANGE,
                    NotificationConfig.SMS_ROUTING_KEY,
                    notification
            );
            log.info("SMS notification queued for: {}", notification.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to queue SMS notification", e);
            throw new RuntimeException("Failed to queue SMS notification", e);
        }
    }

    public void sendVerificationSms(String phoneNumber, String code) {
        SmsNotification notification = SmsNotification.builder()
                .phoneNumber(phoneNumber)
                .message(String.format("Tu código de verificación BusConnect es: %s. Válido por 10 minutos.", code))
                .type("VERIFICATION")
                .build();

        sendSmsNotification(notification);
    }

    public void sendTripReminderSms(String phoneNumber, String tripInfo) {
        SmsNotification notification = SmsNotification.builder()
                .phoneNumber(phoneNumber)
                .message(String.format("Recordatorio: Tu viaje %s sale pronto. ¡Buen viaje!", tripInfo))
                .type("ALERT")
                .build();

        sendSmsNotification(notification);
    }

    // ==================== WHATSAPP NOTIFICATIONS ====================

    public void sendWhatsAppNotification(WhatsAppNotification notification) {
        try {
            rabbitTemplate.convertAndSend(
                    NotificationConfig.NOTIFICATION_EXCHANGE,
                    NotificationConfig.WHATSAPP_ROUTING_KEY,
                    notification
            );
            log.info("WhatsApp notification queued for: {}", notification.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to queue WhatsApp notification", e);
            throw new RuntimeException("Failed to queue WhatsApp notification", e);
        }
    }

    public void sendWhatsAppVerification(String phoneNumber, String code) {
        WhatsAppNotification notification = WhatsAppNotification.builder()
                .phoneNumber(phoneNumber)
                .templateName("verification-code")
                .message(String.format("🚌 *BusConnect*\n\nTu código de verificación es: *%s*\n\nVálido por 10 minutos.", code))
                .variables(Map.of("code", code))
                .build();

        sendWhatsAppNotification(notification);
    }

    public void sendWhatsAppTicket(String phoneNumber, String ticketQR, String tripInfo) {
        WhatsAppNotification notification = WhatsAppNotification.builder()
                .phoneNumber(phoneNumber)
                .templateName("ticket-confirmation")
                .message(String.format("🎫 *Ticket Confirmado*\n\n%s\n\nCódigo QR: %s", tripInfo, ticketQR))
                .variables(Map.of(
                        "ticketQR", ticketQR,
                        "tripInfo", tripInfo
                ))
                .build();

        sendWhatsAppNotification(notification);
    }

    public void sendPlatformChangeAlert(String phoneNumber, String oldPlatform, String newPlatform) {
        WhatsAppNotification notification = WhatsAppNotification.builder()
                .phoneNumber(phoneNumber)
                .templateName("platform-change")
                .message(String.format("*Cambio de Andén*\n\nAndén anterior: %s\nNuevo andén: %s", oldPlatform, newPlatform))
                .variables(Map.of(
                        "oldPlatform", oldPlatform,
                        "newPlatform", newPlatform
                ))
                .build();

        sendWhatsAppNotification(notification);
    }
}

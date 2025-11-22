package com.bers.security.config;

import com.bers.security.msg.NotificationDtos.EmailNotification;
import com.bers.security.msg.NotificationDtos.NotificationResponse;
import com.bers.security.msg.NotificationDtos.SmsNotification;
import com.bers.security.msg.NotificationDtos.WhatsAppNotification;
import com.bers.security.msg.NotificationProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationProducer notificationProducer;

    // ==================== EMAIL ENDPOINTS ====================

    @PostMapping("/email/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<NotificationResponse> sendEmail(@Valid @RequestBody EmailNotification notification) {
        log.info("Sending email notification to: {}", notification.getTo());

        try {
            notificationProducer.sendEmailNotification(notification);
            return ResponseEntity.ok(
                    NotificationResponse.success("Email notification queued successfully")
            );
        } catch (Exception e) {
            log.error("Failed to queue email notification", e);
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue email notification: " + e.getMessage())
            );
        }
    }

    @PostMapping("/email/welcome")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERK')")
    public ResponseEntity<NotificationResponse> sendWelcomeEmail(
            @RequestParam String email,
            @RequestParam String username
    ) {
        log.info("Sending welcome email to: {}", email);

        try {
            notificationProducer.sendWelcomeEmail(email, username);
            return ResponseEntity.ok(
                    NotificationResponse.success("Welcome email queued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue welcome email: " + e.getMessage())
            );
        }
    }

    @PostMapping("/email/verification")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERK')")
    public ResponseEntity<NotificationResponse> sendVerificationEmail(
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String code
    ) {
        log.info("Sending verification email to: {}", email);

        try {
            notificationProducer.sendVerificationEmail(email, username, code);
            return ResponseEntity.ok(
                    NotificationResponse.success("Verification email queued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue verification email: " + e.getMessage())
            );
        }
    }

    @PostMapping("/email/ticket-confirmation")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<NotificationResponse> sendTicketConfirmation(
            @RequestParam String email,
            @RequestParam String username,
            @RequestBody Map<String, Object> ticketData
    ) {
        log.info("Sending ticket confirmation to: {}", email);

        try {
            notificationProducer.sendTicketConfirmation(email, username, ticketData);
            return ResponseEntity.ok(
                    NotificationResponse.success("Ticket confirmation queued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue ticket confirmation: " + e.getMessage())
            );
        }
    }

    // ==================== SMS ENDPOINTS ====================

    @PostMapping("/sms/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<NotificationResponse> sendSms(@Valid @RequestBody SmsNotification notification) {
        log.info("Sending SMS notification to: {}", notification.getPhoneNumber());

        try {
            notificationProducer.sendSmsNotification(notification);
            return ResponseEntity.ok(
                    NotificationResponse.success("SMS notification queued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue SMS notification: " + e.getMessage())
            );
        }
    }

    @PostMapping("/sms/verification")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERK')")
    public ResponseEntity<NotificationResponse> sendVerificationSms(
            @RequestParam String phoneNumber,
            @RequestParam String code
    ) {
        log.info("Sending verification SMS to: {}", phoneNumber);

        try {
            notificationProducer.sendVerificationSms(phoneNumber, code);
            return ResponseEntity.ok(
                    NotificationResponse.success("Verification SMS queued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue verification SMS: " + e.getMessage())
            );
        }
    }

    @PostMapping("/sms/trip-reminder")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<NotificationResponse> sendTripReminderSms(
            @RequestParam String phoneNumber,
            @RequestParam String tripInfo
    ) {
        log.info("Sending trip reminder SMS to: {}", phoneNumber);

        try {
            notificationProducer.sendTripReminderSms(phoneNumber, tripInfo);
            return ResponseEntity.ok(
                    NotificationResponse.success("Trip reminder SMS queued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue trip reminder SMS: " + e.getMessage())
            );
        }
    }

    // ==================== WHATSAPP ENDPOINTS ====================

    @PostMapping("/whatsapp/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
    public ResponseEntity<NotificationResponse> sendWhatsApp(@Valid @RequestBody WhatsAppNotification notification) {
        log.info("Sending WhatsApp notification to: {}", notification.getPhoneNumber());

        try {
            notificationProducer.sendWhatsAppNotification(notification);
            return ResponseEntity.ok(
                    NotificationResponse.success("WhatsApp notification queued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue WhatsApp notification: " + e.getMessage())
            );
        }
    }

    @PostMapping("/whatsapp/verification")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERK')")
    public ResponseEntity<NotificationResponse> sendVerificationWhatsApp(
            @RequestParam String phoneNumber,
            @RequestParam String code
    ) {
        log.info("Sending verification WhatsApp to: {}", phoneNumber);

        try {
            notificationProducer.sendWhatsAppVerification(phoneNumber, code);
            return ResponseEntity.ok(
                    NotificationResponse.success("Verification WhatsApp queued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue verification WhatsApp: " + e.getMessage())
            );
        }
    }

    @PostMapping("/whatsapp/ticket")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<NotificationResponse> sendWhatsAppTicket(
            @RequestParam String phoneNumber,
            @RequestParam String ticketQR,
            @RequestParam String tripInfo
    ) {
        log.info("Sending ticket WhatsApp to: {}", phoneNumber);

        try {
            notificationProducer.sendWhatsAppTicket(phoneNumber, ticketQR, tripInfo);
            return ResponseEntity.ok(
                    NotificationResponse.success("Ticket WhatsApp queued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue ticket WhatsApp: " + e.getMessage())
            );
        }
    }

    @PostMapping("/whatsapp/platform-change")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<NotificationResponse> sendPlatformChangeAlert(
            @RequestParam String phoneNumber,
            @RequestParam String oldPlatform,
            @RequestParam String newPlatform
    ) {
        log.info("Sending platform change WhatsApp to: {}", phoneNumber);

        try {
            notificationProducer.sendPlatformChangeAlert(phoneNumber, oldPlatform, newPlatform);
            return ResponseEntity.ok(
                    NotificationResponse.success("Platform change alert queued successfully")
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    NotificationResponse.error("Failed to queue platform change alert: " + e.getMessage())
            );
        }
    }
}

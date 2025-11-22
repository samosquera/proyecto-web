package com.bers.security.msg;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
class WhatsAppService {

    @Value("${twilio.whatsapp-from}")
    private String whatsappFrom;

    @Retryable(
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendWhatsApp(String to, String body) {
        try {
            log.info("Sending WhatsApp to: {} with message: {}", maskPhone(to), body);

            // Asegurar formato correcto
            String formattedTo = to.startsWith("whatsapp:") ? to : "whatsapp:" + to;

            Message message = Message.creator(
                    new PhoneNumber(formattedTo),
                    new PhoneNumber(whatsappFrom),
                    body
            ).create();

            log.info("WhatsApp sent successfully. SID: {}, Status: {}",
                    message.getSid(), message.getStatus());

        } catch (Exception e) {
            log.error("Failed to send WhatsApp to: {}", maskPhone(to), e);
            throw new RuntimeException("WhatsApp sending failed: " + e.getMessage(), e);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }
}


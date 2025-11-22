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
public class SmsService {

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @Retryable(
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void sendSms(String to, String body) {
        try {
            log.info("Sending SMS to: {} with message: {}", maskPhone(to), body);

            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromPhoneNumber),
                    body
            ).create();

            log.info("SMS sent successfully. SID: {}, Status: {}",
                    message.getSid(), message.getStatus());

        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", maskPhone(to), e);
            throw new RuntimeException("SMS sending failed: " + e.getMessage(), e);
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) return "***";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
    }
}

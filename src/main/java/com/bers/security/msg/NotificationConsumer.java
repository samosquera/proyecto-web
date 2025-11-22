package com.bers.security.msg;

import com.bers.security.msg.NotificationDtos.EmailNotification;
import com.bers.security.msg.NotificationDtos.SmsNotification;
import com.bers.security.msg.NotificationDtos.WhatsAppNotification;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
class NotificationConsumer {

    private final EmailService emailService;
    private final SmsService smsService;
    private final WhatsAppService whatsAppService;

    @RabbitListener(queues = NotificationConfig.EMAIL_QUEUE)
    public void consumeEmailNotification(EmailNotification notification,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                         Channel channel) {
        try {
            log.info("Processing email notification to: {}", notification.getTo());
            emailService.sendEmail(notification);
            log.info("Email sent successfully to: {}", notification.getTo());

            // Acknowledge manual
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Failed to process email notification", e);
            try {
                // Rechazar y reenviar a DLQ
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioe) {
                log.error("Failed to nack message", ioe);
            }
        }
    }

    @RabbitListener(queues = NotificationConfig.SMS_QUEUE)
    public void consumeSmsNotification(SmsNotification notification,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                       Channel channel) {
        try {
            log.info("Processing SMS notification to: {}", notification.getPhoneNumber());
            smsService.sendSms(notification.getPhoneNumber(), notification.getMessage());
            log.info("SMS sent successfully to: {}", notification.getPhoneNumber());

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Failed to process SMS notification", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioe) {
                log.error("Failed to nack message", ioe);
            }
        }
    }

    @RabbitListener(queues = NotificationConfig.WHATSAPP_QUEUE)
    public void consumeWhatsAppNotification(WhatsAppNotification notification,
                                            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                                            Channel channel) {
        try {
            log.info("Processing WhatsApp notification to: {}", notification.getPhoneNumber());
            whatsAppService.sendWhatsApp(notification.getPhoneNumber(), notification.getMessage());
            log.info("WhatsApp sent successfully to: {}", notification.getPhoneNumber());

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("Failed to process WhatsApp notification", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioe) {
                log.error("Failed to nack message", ioe);
            }
        }
    }
}

package com.bers.mensajeria;

import com.bers.security.msg.NotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class NotificationControllerTest {
    private final NotificationProducer producer;

    @GetMapping("/send")
    public String sendTestNotifications() {

        // Enviar email
        producer.sendWelcomeEmail("rizoandres24@gmail.com", "Andres");

        // Enviar SMS
        producer.sendVerificationSms("+573226933590", "338974");

        // Enviar WhatsApp
        producer.sendWhatsAppVerification("+573226933590", "338974");

        return "Mensajes enviados a la cola. Revisa logs y tus dispositivos";
    }

}
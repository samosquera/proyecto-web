package com.bers.security.msg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class NotificationConfig {

    // Nombres de colas
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String SMS_QUEUE = "sms.queue";
    public static final String WHATSAPP_QUEUE = "whatsapp.queue";

    // Dead Letter Queues
    public static final String DLQ_EMAIL = "dlq.email.queue";
    public static final String DLQ_SMS = "dlq.sms.queue";
    public static final String DLQ_WHATSAPP = "dlq.whatsapp.queue";

    // Exchanges
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    // Routing keys
    public static final String EMAIL_ROUTING_KEY = "notification.email";
    public static final String SMS_ROUTING_KEY = "notification.sms";
    public static final String WHATSAPP_ROUTING_KEY = "notification.whatsapp";
    // ==================== QUEUES CON DLQ ====================

    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.email")
                .withArgument("x-message-ttl", 60000) // 1 minuto TTL
                .build();
    }

    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(SMS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.sms")
                .withArgument("x-message-ttl", 60000)
                .build();
    }

    @Bean
    public Queue whatsappQueue() {
        return QueueBuilder.durable(WHATSAPP_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "dlq.whatsapp")
                .withArgument("x-message-ttl", 60000)
                .build();
    }

    // Dead Letter Queues
    @Bean
    public Queue dlqEmail() {
        return QueueBuilder.durable(DLQ_EMAIL).build();
    }

    @Bean
    public Queue dlqSms() {
        return QueueBuilder.durable(DLQ_SMS).build();
    }

    @Bean
    public Queue dlqWhatsapp() {
        return QueueBuilder.durable(DLQ_WHATSAPP).build();
    }

    // ==================== EXCHANGES ====================

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(DLX_EXCHANGE, true, false);
    }

    // ==================== BINDINGS ====================

    @Bean
    public Binding emailBinding() {
        return BindingBuilder
                .bind(emailQueue())
                .to(notificationExchange())
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding smsBinding() {
        return BindingBuilder
                .bind(smsQueue())
                .to(notificationExchange())
                .with(SMS_ROUTING_KEY);
    }

    @Bean
    public Binding whatsappBinding() {
        return BindingBuilder
                .bind(whatsappQueue())
                .to(notificationExchange())
                .with(WHATSAPP_ROUTING_KEY);
    }

    // DLQ Bindings
    @Bean
    public Binding dlqEmailBinding() {
        return BindingBuilder
                .bind(dlqEmail())
                .to(dlxExchange())
                .with("dlq.email");
    }

    @Bean
    public Binding dlqSmsBinding() {
        return BindingBuilder
                .bind(dlqSms())
                .to(dlxExchange())
                .with("dlq.sms");
    }

    @Bean
    public Binding dlqWhatsappBinding() {
        return BindingBuilder
                .bind(dlqWhatsapp())
                .to(dlxExchange())
                .with("dlq.whatsapp");
    }

    // ==================== INICIALIZAR TODAS LAS COLAS ====================

    @Bean
    public Declarables queuesAndExchanges() {
        return new Declarables(
                // Queues
                emailQueue(),
                smsQueue(),
                whatsappQueue(),
                dlqEmail(),
                dlqSms(),
                dlqWhatsapp(),

                // Exchanges
                notificationExchange(),
                dlxExchange(),

                // Bindings
                emailBinding(),
                smsBinding(),
                whatsappBinding(),
                dlqEmailBinding(),
                dlqSmsBinding(),
                dlqWhatsappBinding()
        );
    }

    // ==================== MESSAGE CONVERTER ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return new Jackson2JsonMessageConverter(mapper);
    }

    // ==================== RABBIT TEMPLATE CON RETRY ====================

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());

        // Configurar retry
        RetryTemplate retryTemplate = new RetryTemplate();

        // Política de reintentos: 3 intentos
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Backoff exponencial: 1s, 2s, 4s
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        template.setRetryTemplate(retryTemplate);

        return template;
    }
}

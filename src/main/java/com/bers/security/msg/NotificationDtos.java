package com.bers.security.msg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

public class NotificationDtos {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailNotification implements Serializable {
        private String to;
        private String subject;
        private String templateName;
        private Map<String, Object> variables;
        @Default
        private LocalDateTime sentAt = LocalDateTime.now();

        public EmailNotification(String to, String subject, String templateName, Map<String, Object> variables) {
            this.to = to;
            this.subject = subject;
            this.templateName = templateName;
            this.variables = variables;
            this.sentAt = LocalDateTime.now();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsNotification implements Serializable {
        private String phoneNumber;
        private String message;
        private String type; // VERIFICATION, ALERT, INFO
        private LocalDateTime sentAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhatsAppNotification implements Serializable {
        private String phoneNumber;
        private String message;
        private String templateName;
        private Map<String, Object> variables;
        private LocalDateTime sentAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private boolean success;
        private String message;
        private LocalDateTime timestamp;

        public static NotificationResponse success(String message) {
            return NotificationResponse.builder()
                    .success(true)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static NotificationResponse error(String message) {
            return NotificationResponse.builder()
                    .success(false)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}

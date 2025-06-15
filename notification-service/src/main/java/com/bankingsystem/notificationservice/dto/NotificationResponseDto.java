package com.bankingsystem.notificationservice.dto;

import com.bankingsystem.notificationservice.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private Long id;
    private Long userId;
    private String subject;
    private String content;
    private NotificationType type;
    private String recipient;
    private Boolean sent;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
    private String errorMessage;
    private String message;

    public String getMessage() {
        return message != null ? message : content;
    }
}

package com.bankingsystem.notificationservice.dto;

import com.bankingsystem.notificationservice.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Notification type is required")
    private NotificationType type;
    
    @NotBlank(message = "Recipient is required")
    private String recipient;
    
    private String message;
    
    public String getMessage() {
        // Return message if set, otherwise return content
        return message != null ? message : content;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

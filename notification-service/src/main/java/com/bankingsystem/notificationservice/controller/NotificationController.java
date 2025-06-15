package com.bankingsystem.notificationservice.controller;

import com.bankingsystem.notificationservice.dto.NotificationRequest;
import com.bankingsystem.notificationservice.dto.NotificationResponseDto;
import com.bankingsystem.notificationservice.model.NotificationType;
import com.bankingsystem.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponseDto>> getAllNotifications(Pageable pageable) {
        Page<NotificationResponseDto> notifications = notificationService.getAllNotifications(pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationsByUserId(
            @PathVariable Long userId, Pageable pageable) {
        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByUserId(userId, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationsByType(
            @PathVariable NotificationType type, Pageable pageable) {
        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByType(type, pageable);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationsByUserIdAndType(
            @PathVariable Long userId, @PathVariable NotificationType type, Pageable pageable) {
        Page<NotificationResponseDto> notifications = notificationService.getNotificationsByUserIdAndType(userId, type, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping
    public ResponseEntity<NotificationResponseDto> createNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponseDto notification = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }
    
    @PostMapping("/resend-pending")
    public ResponseEntity<Void> resendPendingNotifications() {
        notificationService.sendPendingNotifications();
        return ResponseEntity.ok().build();
    }
}

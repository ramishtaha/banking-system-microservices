package com.bankingsystem.notificationservice.service;

import com.bankingsystem.notificationservice.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);
    
    @Async
    public void sendPushNotification(Notification notification) {
        // In a real application, this would integrate with a push notification service like Firebase Cloud Messaging
        logger.info("Sending push notification to: {}", notification.getRecipient());
        
        try {
            // Simulate push notification delay
            Thread.sleep(100);
            logger.info("Push notification sent to: {}", notification.getRecipient());
        } catch (InterruptedException e) {
            logger.error("Push notification sending interrupted", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to send push notification", e);
        }
    }
}

package com.bankingsystem.notificationservice.service;

import com.bankingsystem.notificationservice.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    
    @Async
    public void sendSms(Notification notification) {
        // In a real application, this would integrate with an SMS provider like Twilio
        logger.info("Sending SMS to: {}", notification.getRecipient());
        
        try {
            // Simulate SMS sending delay
            Thread.sleep(200);
            logger.info("SMS sent to: {}", notification.getRecipient());
        } catch (InterruptedException e) {
            logger.error("SMS sending interrupted", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to send SMS", e);
        }
    }
}

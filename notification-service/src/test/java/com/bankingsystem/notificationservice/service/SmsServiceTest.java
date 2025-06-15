package com.bankingsystem.notificationservice.service;

import com.bankingsystem.notificationservice.model.Notification;
import com.bankingsystem.notificationservice.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @InjectMocks
    private SmsService smsService;

    private Notification testNotification;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        testNotification = Notification.builder()
                .id(1L)
                .userId(1L)
                .subject("Test SMS")
                .content("This is a test SMS content")
                .type(NotificationType.SMS)
                .recipient("+1234567890")
                .sent(false)
                .createdAt(now)
                .build();
    }

    @Test
    void sendSms_shouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> smsService.sendSms(testNotification));
    }

    @Test
    void sendSms_shouldHandleInterruptedException() throws InterruptedException {
        // Create a subclass of SmsService that will throw InterruptedException
        SmsService testService = new SmsService() {
            @Override
            public void sendSms(Notification notification) {
                // Immediately interrupt the thread to simulate InterruptedException
                Thread.currentThread().interrupt();
                super.sendSms(notification);
            }
        };
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> testService.sendSms(testNotification));
    }
}

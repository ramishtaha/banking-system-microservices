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
class PushNotificationServiceTest {

    @InjectMocks
    private PushNotificationService pushNotificationService;

    private Notification testNotification;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        testNotification = Notification.builder()
                .id(1L)
                .userId(1L)
                .subject("Test Push")
                .content("This is a test push notification content")
                .type(NotificationType.PUSH)
                .recipient("device-token-123")
                .sent(false)
                .createdAt(now)
                .build();
    }

    @Test
    void sendPushNotification_shouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> pushNotificationService.sendPushNotification(testNotification));
    }

    @Test
    void sendPushNotification_shouldHandleInterruptedException() throws InterruptedException {
        // Create a subclass of PushNotificationService that will throw InterruptedException
        PushNotificationService testService = new PushNotificationService() {
            @Override
            public void sendPushNotification(Notification notification) {
                // Immediately interrupt the thread to simulate InterruptedException
                Thread.currentThread().interrupt();
                super.sendPushNotification(notification);
            }
        };
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> testService.sendPushNotification(testNotification));
    }
}

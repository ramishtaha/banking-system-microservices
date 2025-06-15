package com.bankingsystem.notificationservice.service;

import com.bankingsystem.notificationservice.model.Notification;
import com.bankingsystem.notificationservice.model.NotificationType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private Notification testNotification;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        testNotification = Notification.builder()
                .id(1L)
                .userId(1L)
                .subject("Test Email Subject")
                .content("<p>This is a test email content</p>")
                .type(NotificationType.EMAIL)
                .recipient("test@example.com")
                .sent(false)
                .createdAt(now)
                .build();
    }

    @Test
    void sendEmail_shouldSendSuccessfully() throws MessagingException {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        // Act
        assertDoesNotThrow(() -> emailService.sendEmail(testNotification));
        
        // Assert
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }    @Test
    void sendEmail_shouldThrowExceptionWhenSendingFails() throws MessagingException {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> emailService.sendEmail(testNotification));
        
        // Verify
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }
}

package com.bankingsystem.notificationservice.service;

import com.bankingsystem.notificationservice.dto.NotificationRequest;
import com.bankingsystem.notificationservice.dto.NotificationResponseDto;
import com.bankingsystem.notificationservice.model.Notification;
import com.bankingsystem.notificationservice.model.NotificationType;
import com.bankingsystem.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private EmailService emailService;
    
    @Mock
    private SmsService smsService;
    
    @Mock
    private PushNotificationService pushNotificationService;
    
    @InjectMocks
    private NotificationService notificationService;
    
    private Notification testNotification;
    private List<Notification> notifications;
    private Page<Notification> notificationPage;
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        testNotification = Notification.builder()
                .id(1L)
                .userId(1L)
                .subject("Test Subject")
                .content("Test Content")
                .type(NotificationType.EMAIL)
                .recipient("user@example.com")
                .sent(true)
                .sentAt(now)
                .createdAt(now)
                .build();
                
        notifications = Arrays.asList(testNotification);
        notificationPage = new PageImpl<>(notifications);
    }
    
    @Test
    void getAllNotifications_shouldReturnAllNotifications() {
        // Arrange
        when(notificationRepository.findAll(any(Pageable.class))).thenReturn(notificationPage);
        
        // Act
        Page<NotificationResponseDto> result = notificationService.getAllNotifications(PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Subject", result.getContent().get(0).getSubject());
        verify(notificationRepository).findAll(any(Pageable.class));
    }
    
    @Test
    void getNotificationsByUserId_shouldReturnUserNotifications() {
        // Arrange
        when(notificationRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(notificationPage);
        
        // Act
        Page<NotificationResponseDto> result = notificationService.getNotificationsByUserId(1L, PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getUserId());
        verify(notificationRepository).findByUserId(eq(1L), any(Pageable.class));
    }
    
    @Test
    void getNotificationsByType_shouldReturnTypedNotifications() {
        // Arrange
        when(notificationRepository.findByType(eq(NotificationType.EMAIL), any(Pageable.class)))
                .thenReturn(notificationPage);
        
        // Act
        Page<NotificationResponseDto> result = notificationService.getNotificationsByType(
                NotificationType.EMAIL, PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(NotificationType.EMAIL, result.getContent().get(0).getType());
        verify(notificationRepository).findByType(eq(NotificationType.EMAIL), any(Pageable.class));
    }
    
    @Test
    void getNotificationsByUserIdAndType_shouldReturnFilteredNotifications() {
        // Arrange
        when(notificationRepository.findByUserIdAndType(eq(1L), eq(NotificationType.EMAIL), any(Pageable.class)))
                .thenReturn(notificationPage);
        
        // Act
        Page<NotificationResponseDto> result = notificationService.getNotificationsByUserIdAndType(
                1L, NotificationType.EMAIL, PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getUserId());
        assertEquals(NotificationType.EMAIL, result.getContent().get(0).getType());
        verify(notificationRepository).findByUserIdAndType(eq(1L), eq(NotificationType.EMAIL), any(Pageable.class));
    }
    
    @Test
    void createNotification_email_shouldCreateAndSendNotification() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setType(NotificationType.EMAIL);
        request.setRecipient("user@example.com");
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doNothing().when(emailService).sendEmail(any(Notification.class));
        
        // Act
        NotificationResponseDto result = notificationService.createNotification(request);
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals(request.getUserId(), result.getUserId());
        assertEquals(request.getSubject(), result.getSubject());
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService).sendEmail(any(Notification.class));
    }
    
    @Test
    void createNotification_sms_shouldCreateAndSendNotification() {
        // Arrange
        testNotification.setType(NotificationType.SMS);
        
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setType(NotificationType.SMS);
        request.setRecipient("+1234567890");
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        doNothing().when(smsService).sendSms(any(Notification.class));
        
        // Act
        NotificationResponseDto result = notificationService.createNotification(request);
        
        // Assert
        assertEquals(1L, result.getId());
        assertEquals(NotificationType.SMS, result.getType());
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(smsService).sendSms(any(Notification.class));
    }
    
    @Test
    void sendPendingNotifications_shouldProcessAndUpdateNotifications() {
        // Arrange
        Notification pendingNotification = Notification.builder()
                .id(2L)
                .userId(2L)
                .subject("Pending Subject")
                .content("Pending Content")
                .type(NotificationType.EMAIL)
                .recipient("pending@example.com")
                .sent(false)
                .createdAt(now.minusDays(1))
                .build();
                
        when(notificationRepository.findBySentFalse()).thenReturn(Arrays.asList(pendingNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(pendingNotification);
        
        // Act
        notificationService.sendPendingNotifications();
        
        // Assert
        verify(notificationRepository).findBySentFalse();
        verify(emailService).sendEmail(pendingNotification);
        verify(notificationRepository).save(pendingNotification);
    }
    
    @Test
    void sendPendingNotifications_shouldHandleExceptions() {
        // Arrange
        Notification emailNotification = Notification.builder()
                .id(2L)
                .userId(2L)
                .subject("Email Subject")
                .content("Email Content")
                .type(NotificationType.EMAIL)
                .recipient("email@example.com")
                .sent(false)
                .createdAt(now.minusDays(1))
                .build();
                
        Notification smsNotification = Notification.builder()
                .id(3L)
                .userId(2L)
                .subject("SMS Subject")
                .content("SMS Content")
                .type(NotificationType.SMS)
                .recipient("+1234567890")
                .sent(false)
                .createdAt(now.minusDays(1))
                .build();
                
        when(notificationRepository.findBySentFalse()).thenReturn(Arrays.asList(emailNotification, smsNotification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);
        doThrow(new RuntimeException("Email service error")).when(emailService).sendEmail(any(Notification.class));
        
        // Act
        notificationService.sendPendingNotifications();
        
        // Assert
        verify(notificationRepository).findBySentFalse();
        verify(emailService).sendEmail(any(Notification.class));
        verify(smsService).sendSms(any(Notification.class));
        
        // Verify the error was recorded
        verify(notificationRepository, times(2)).save(argThat(notification -> {
            if (notification.getId().equals(2L)) {
                return notification.getErrorMessage() != null && 
                       notification.getErrorMessage().contains("Email service error") &&
                       !notification.getSent();
            }
            return true;
        }));
    }
    
    @Test
    void createNotification_shouldHandleInvalidType() {
        // Arrange
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setSubject("Test Subject");
        request.setContent("Test Content");
        request.setType(NotificationType.ACCOUNT_NOTIFICATION); // Type that doesn't have a sender implementation
        request.setRecipient("user@example.com");
        
        Notification savedNotification = Notification.builder()
                .id(1L)
                .userId(1L)
                .subject("Test Subject")
                .content("Test Content")
                .type(NotificationType.ACCOUNT_NOTIFICATION)
                .recipient("user@example.com")
                .sent(false)
                .createdAt(now)
                .build();
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        
        // Act & Assert
        NotificationResponseDto result = notificationService.createNotification(request);
        
        // The service should handle the exception and set error message
        assertEquals("Test Subject", result.getSubject());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("Unsupported notification type"));
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
}

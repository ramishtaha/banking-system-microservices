package com.bankingsystem.notificationservice.controller;

import com.bankingsystem.notificationservice.dto.NotificationRequest;
import com.bankingsystem.notificationservice.dto.NotificationResponseDto;
import com.bankingsystem.notificationservice.model.NotificationType;
import com.bankingsystem.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationResponseDto testNotification;
    private NotificationRequest notificationRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        testNotification = NotificationResponseDto.builder()
                .id(1L)
                .userId(1L)
                .type(NotificationType.ACCOUNT_ACTIVITY)
                .message("Test notification message")
                .sent(true)
                .sentAt(now)
                .build();

        notificationRequest = new NotificationRequest();
        notificationRequest.setUserId(1L);
        notificationRequest.setType(NotificationType.ACCOUNT_ACTIVITY);
        notificationRequest.setMessage("Test notification message");
    }

    @Test
    void getAllNotifications_shouldReturnAllNotifications() {
        // Arrange
        List<NotificationResponseDto> notifications = Arrays.asList(testNotification);
        Page<NotificationResponseDto> page = new PageImpl<>(notifications);
        
        when(notificationService.getAllNotifications(any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<NotificationResponseDto>> response = 
            notificationController.getAllNotifications(Pageable.unpaged());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(notificationService).getAllNotifications(any(Pageable.class));
    }

    @Test
    void getNotificationsByUserId_shouldReturnUserNotifications() {
        // Arrange
        Long userId = 1L;
        List<NotificationResponseDto> notifications = Arrays.asList(testNotification);
        Page<NotificationResponseDto> page = new PageImpl<>(notifications);
        
        when(notificationService.getNotificationsByUserId(eq(userId), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<NotificationResponseDto>> response = 
            notificationController.getNotificationsByUserId(userId, Pageable.unpaged());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(userId, response.getBody().getContent().get(0).getUserId());
        verify(notificationService).getNotificationsByUserId(eq(userId), any(Pageable.class));
    }

    @Test
    void getNotificationsByType_shouldReturnTypedNotifications() {
        // Arrange
        NotificationType type = NotificationType.ACCOUNT_ACTIVITY;
        List<NotificationResponseDto> notifications = Arrays.asList(testNotification);
        Page<NotificationResponseDto> page = new PageImpl<>(notifications);
        
        when(notificationService.getNotificationsByType(eq(type), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<NotificationResponseDto>> response = 
            notificationController.getNotificationsByType(type, Pageable.unpaged());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(type, response.getBody().getContent().get(0).getType());
        verify(notificationService).getNotificationsByType(eq(type), any(Pageable.class));
    }

    @Test
    void getNotificationsByUserIdAndType_shouldReturnFilteredNotifications() {
        // Arrange
        Long userId = 1L;
        NotificationType type = NotificationType.ACCOUNT_ACTIVITY;
        List<NotificationResponseDto> notifications = Arrays.asList(testNotification);
        Page<NotificationResponseDto> page = new PageImpl<>(notifications);
        
        when(notificationService.getNotificationsByUserIdAndType(
                eq(userId), eq(type), any(Pageable.class))).thenReturn(page);

        // Act
        ResponseEntity<Page<NotificationResponseDto>> response = 
            notificationController.getNotificationsByUserIdAndType(userId, type, Pageable.unpaged());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(userId, response.getBody().getContent().get(0).getUserId());
        assertEquals(type, response.getBody().getContent().get(0).getType());
        verify(notificationService).getNotificationsByUserIdAndType(eq(userId), eq(type), any(Pageable.class));
    }

    @Test
    void createNotification_shouldReturnCreatedNotification() {
        // Arrange
        when(notificationService.createNotification(any(NotificationRequest.class))).thenReturn(testNotification);

        // Act
        ResponseEntity<NotificationResponseDto> response = 
            notificationController.createNotification(notificationRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testNotification.getId(), response.getBody().getId());
        verify(notificationService).createNotification(notificationRequest);
    }

    @Test
    void resendPendingNotifications_shouldCallService() {
        // Act
        ResponseEntity<Void> response = notificationController.resendPendingNotifications();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(notificationService).sendPendingNotifications();
    }
}

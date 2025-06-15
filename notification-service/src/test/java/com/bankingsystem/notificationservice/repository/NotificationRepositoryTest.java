package com.bankingsystem.notificationservice.repository;

import com.bankingsystem.notificationservice.model.Notification;
import com.bankingsystem.notificationservice.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification notification1;
    private Notification notification2;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        
        now = LocalDateTime.now();
          notification1 = Notification.builder()
                .userId(1L)
                .subject("Test Subject 1")
                .content("Test Content 1")
                .type(NotificationType.EMAIL)
                .recipient("user1@example.com")
                .sent(true)
                .sentAt(now)
                .createdAt(now)
                .build();

        notification2 = Notification.builder()
                .userId(2L)
                .subject("Test Subject 2")
                .content("Test Content 2")
                .type(NotificationType.SMS)
                .recipient("user2@example.com")
                .sent(false)
                .createdAt(now.minusDays(1))
                .build();
                
        notificationRepository.saveAll(List.of(notification1, notification2));
    }

    @Test
    void findByUserId_shouldReturnUserNotifications() {
        // Act
        Page<Notification> result = notificationRepository.findByUserId(1L, PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Subject 1", result.getContent().get(0).getSubject());
    }
    
    @Test
    void findBySentFalse_shouldReturnPendingNotifications() {
        // Act
        List<Notification> result = notificationRepository.findBySentFalse();
        
        // Assert
        assertEquals(1, result.size());
        assertEquals("Test Subject 2", result.get(0).getSubject());
        assertFalse(result.get(0).getSent());
    }      @Test
    void findByType_shouldReturnTypedNotifications() {
        // Act
        Page<Notification> result = notificationRepository.findByType(
                NotificationType.EMAIL, PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(NotificationType.EMAIL, result.getContent().get(0).getType());
    }
      @Test
    void findByCreatedAtBetween_shouldReturnNotificationsInTimeRange() {
        // Act
        Page<Notification> result = notificationRepository.findByCreatedAtBetween(
                now.minusDays(2), now.plusDays(1), PageRequest.of(0, 10));
        
        // Assert
        assertEquals(2, result.getTotalElements());
        
        result = notificationRepository.findByCreatedAtBetween(
                now.minusHours(1), now.plusHours(1), PageRequest.of(0, 10));
        
        // Should only include the first notification which was created 'now'
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Subject 1", result.getContent().get(0).getSubject());
    }
    
    @Test
    void saveNotification_shouldCreateAndUpdateFields() {
        // Arrange
        Notification notification = Notification.builder()
                .userId(3L)
                .subject("New Notification")
                .content("New Content")
                .type(NotificationType.PUSH)
                .recipient("user3@example.com")
                .build();
        
        // Act
        Notification saved = notificationRepository.save(notification);
        
        // Assert
        assertNotNull(saved.getId());
        assertEquals(3L, saved.getUserId());
        assertEquals("New Notification", saved.getSubject());
        assertEquals(NotificationType.PUSH, saved.getType());
        assertNotNull(saved.getCreatedAt());
        assertFalse(saved.getSent()); // Should be set by @PrePersist
    }
      @Test
    void findByUserIdAndType_shouldReturnFilteredNotifications() {
        // Arrange
        Notification notification3 = Notification.builder()
                .userId(1L)
                .subject("Test Subject 3")
                .content("Test Content 3")
                .type(NotificationType.PUSH)
                .recipient("user1@example.com")
                .sent(true)
                .sentAt(now)
                .createdAt(now)
                .build();
        notificationRepository.save(notification3);
        
        // Act
        Page<Notification> result = notificationRepository.findByUserIdAndType(
                1L, NotificationType.PUSH, PageRequest.of(0, 10));
        
        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getUserId());
        assertEquals(NotificationType.PUSH, result.getContent().get(0).getType());
    }
    
    @Test
    void findByCreatedAtBetween_shouldReturnTimeRangeNotifications() {
        // Act
        Page<Notification> result = notificationRepository.findByCreatedAtBetween(
                now.minusDays(2), now.plusDays(1), PageRequest.of(0, 10));
        
        // Assert
        assertEquals(2, result.getTotalElements());
    }
}

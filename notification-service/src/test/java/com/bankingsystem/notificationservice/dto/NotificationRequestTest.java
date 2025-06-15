package com.bankingsystem.notificationservice.dto;

import com.bankingsystem.notificationservice.model.NotificationType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NotificationRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validNotificationRequest_shouldPassValidation() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .userId(1L)
                .subject("Test Subject")
                .content("Test Content")
                .type(NotificationType.EMAIL)
                .recipient("user@example.com")
                .build();

        // Act
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Assert
        assertTrue(violations.isEmpty());
    }

    @Test
    void notificationRequestWithNullUserId_shouldFailValidation() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .subject("Test Subject")
                .content("Test Content")
                .type(NotificationType.EMAIL)
                .recipient("user@example.com")
                .build();

        // Act
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("User ID is required", violations.iterator().next().getMessage());
    }

    @Test
    void notificationRequestWithNullSubject_shouldFailValidation() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .userId(1L)
                .content("Test Content")
                .type(NotificationType.EMAIL)
                .recipient("user@example.com")
                .build();

        // Act
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Subject is required", violations.iterator().next().getMessage());
    }

    @Test
    void notificationRequestWithNullContent_shouldFailValidation() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .userId(1L)
                .subject("Test Subject")
                .type(NotificationType.EMAIL)
                .recipient("user@example.com")
                .build();

        // Act
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Content is required", violations.iterator().next().getMessage());
    }

    @Test
    void notificationRequestWithNullType_shouldFailValidation() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .userId(1L)
                .subject("Test Subject")
                .content("Test Content")
                .recipient("user@example.com")
                .build();

        // Act
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Notification type is required", violations.iterator().next().getMessage());
    }

    @Test
    void notificationRequestWithNullRecipient_shouldFailValidation() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .userId(1L)
                .subject("Test Subject")
                .content("Test Content")
                .type(NotificationType.EMAIL)
                .build();

        // Act
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        // Assert
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Recipient is required", violations.iterator().next().getMessage());
    }
    
    @Test
    void getMessage_shouldReturnCustomMessageIfSet() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .userId(1L)
                .subject("Test Subject")
                .content("Test Content")
                .message("Custom Message")
                .type(NotificationType.EMAIL)
                .recipient("user@example.com")
                .build();
        
        // Act & Assert
        assertEquals("Custom Message", request.getMessage());
    }
    
    @Test
    void getMessage_shouldReturnContentIfMessageNotSet() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .userId(1L)
                .subject("Test Subject")
                .content("Test Content")
                .type(NotificationType.EMAIL)
                .recipient("user@example.com")
                .build();
        
        // Act & Assert
        assertEquals("Test Content", request.getMessage());
    }
}

package com.bankingsystem.notificationservice.kafka;

import com.bankingsystem.notificationservice.client.UserServiceClient;
import com.bankingsystem.notificationservice.dto.NotificationRequest;
import com.bankingsystem.notificationservice.dto.UserDto;
import com.bankingsystem.notificationservice.model.NotificationType;
import com.bankingsystem.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserServiceClient userServiceClient;
    
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private NotificationEventConsumer notificationEventConsumer;

    private UserDto testUser;

    @BeforeEach
    void setUp() {
        testUser = UserDto.builder()
                .id(1L)
                .email("user@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void consumeAccountCreatedEvent_shouldCreateNotification() {
        // Arrange
        String payload = "1";  // Account ID
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);

        // Act
        notificationEventConsumer.consumeAccountCreatedEvent(payload, "account-created");

        // Assert
        verify(notificationService).createNotification(any(NotificationRequest.class));
    }

    @Test
    void consumeAccountDeactivatedEvent_shouldCreateNotification() {
        // Arrange
        String payload = "1";  // Account ID
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);

        // Act
        notificationEventConsumer.consumeAccountDeactivatedEvent(payload, "account-deactivated");

        // Assert
        verify(notificationService).createNotification(any(NotificationRequest.class));
    }

    @Test
    void consumeTransactionEvent_deposit_shouldCreateNotification() {
        // Arrange
        String payload = "1:100:1100";  // AccountId:Amount:NewBalance
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);

        // Act
        notificationEventConsumer.consumeTransactionEvent(payload, "deposit");

        // Assert
        verify(notificationService).createNotification(argThat(request -> 
            request.getType() == NotificationType.ACCOUNT_ACTIVITY && 
            request.getMessage().contains("deposit")
        ));
    }

    @Test
    void consumeTransactionEvent_withdrawal_shouldCreateNotification() {
        // Arrange
        String payload = "1:50:950";  // AccountId:Amount:NewBalance
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);

        // Act
        notificationEventConsumer.consumeTransactionEvent(payload, "withdrawal");

        // Assert
        verify(notificationService).createNotification(argThat(request -> 
            request.getType() == NotificationType.ACCOUNT_ACTIVITY && 
            request.getMessage().contains("withdrawal")
        ));
    }

    @Test
    void consumeTransactionEvent_transfer_shouldCreateNotification() {
        // Arrange
        String payload = "1:2:100:900";  // FromAccountId:ToAccountId:Amount:NewBalance
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);

        // Act
        notificationEventConsumer.consumeTransactionEvent(payload, "transfer");

        // Assert
        verify(notificationService).createNotification(argThat(request -> 
            request.getType() == NotificationType.ACCOUNT_ACTIVITY && 
            request.getMessage().contains("transfer")
        ));
    }

    @Test
    void consumeUserEvent_shouldCreateNotification() {
        // Arrange
        String payload = "1";  // User ID
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);

        // Act
        notificationEventConsumer.consumeUserEvent(payload, "user-created");

        // Assert
        verify(notificationService).createNotification(argThat(request -> 
            request.getType() == NotificationType.ACCOUNT_NOTIFICATION && 
            request.getUserId() == 1L
        ));
    }

    @Test
    void handleError_shouldLogAndSendToDlq() {
        // Arrange
        Exception exception = new RuntimeException("Test exception");
        String payload = "test-payload";
        String topic = "test-topic";

        // Act
        notificationEventConsumer.handleError(exception, payload, topic);

        // Assert
        verify(kafkaTemplate).send(eq("notification-events-dlq"), eq(topic), contains("ERROR"));
    }

    @Test
    void consumeEvents_shouldProcessAccountCreatedEvent() {
        // Arrange
        String message = "account-created:1";
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);

        // Act
        notificationEventConsumer.consumeEvents(message);

        // Assert
        verify(notificationService, atLeastOnce()).createNotification(any(NotificationRequest.class));
    }
      @Test
    void consumeEvents_shouldProcessTransactionCreatedEvent() {
        // Arrange
        // Format should match what the implementation expects: userId,amount,transactionType
        String message = "transaction-created:1,100,DEPOSIT";
        when(userServiceClient.getUserById(anyLong())).thenReturn(testUser);

        // Act
        notificationEventConsumer.consumeEvents(message);

        // Assert
        verify(notificationService, atLeastOnce()).createNotification(any(NotificationRequest.class));
    }

    @Test
    void consumeEvents_shouldHandleInvalidFormat() {
        // Arrange
        String message = "invalid-format";

        // Act
        notificationEventConsumer.consumeEvents(message);

        // Assert
        verify(notificationService, never()).createNotification(any(NotificationRequest.class));
    }

    @Test
    void consumeEvents_shouldHandleUnknownEventType() {
        // Arrange
        String message = "unknown-event:1";

        // Act
        notificationEventConsumer.consumeEvents(message);

        // Assert
        verify(notificationService, never()).createNotification(any(NotificationRequest.class));
    }
    
    @Test
    void consumeEvents_shouldHandleExceptions() {
        // Arrange
        String message = "account-created:1";
        when(userServiceClient.getUserById(anyLong())).thenThrow(new RuntimeException("Test exception"));

        // Act
        notificationEventConsumer.consumeEvents(message);

        // Assert
        verify(notificationService, never()).createNotification(any(NotificationRequest.class));
    }
}

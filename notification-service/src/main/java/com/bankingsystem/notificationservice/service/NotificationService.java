package com.bankingsystem.notificationservice.service;

import com.bankingsystem.notificationservice.dto.NotificationRequest;
import com.bankingsystem.notificationservice.dto.NotificationResponseDto;
import com.bankingsystem.notificationservice.model.Notification;
import com.bankingsystem.notificationservice.model.NotificationType;
import com.bankingsystem.notificationservice.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private PushNotificationService pushNotificationService;
    
    public Page<NotificationResponseDto> getAllNotifications(Pageable pageable) {
        return notificationRepository.findAll(pageable)
                .map(this::mapToResponseDto);
    }
    
    public Page<NotificationResponseDto> getNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable)
                .map(this::mapToResponseDto);
    }
    
    public Page<NotificationResponseDto> getNotificationsByType(NotificationType type, Pageable pageable) {
        return notificationRepository.findByType(type, pageable)
                .map(this::mapToResponseDto);
    }
    
    public Page<NotificationResponseDto> getNotificationsByUserIdAndType(
            Long userId, NotificationType type, Pageable pageable) {
        return notificationRepository.findByUserIdAndType(userId, type, pageable)
                .map(this::mapToResponseDto);
    }
    
    @Transactional
    public NotificationResponseDto createNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .subject(request.getSubject())
                .content(request.getContent())
                .type(request.getType())
                .recipient(request.getRecipient())
                .sent(false)
                .createdAt(LocalDateTime.now())
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send notification asynchronously
        try {
            sendNotification(savedNotification);
            
            // Update notification status
            savedNotification.setSent(true);
            savedNotification.setSentAt(LocalDateTime.now());
            notificationRepository.save(savedNotification);
        } catch (Exception e) {
            logger.error("Failed to send notification: {}", e.getMessage(), e);
            
            savedNotification.setErrorMessage(e.getMessage());
            notificationRepository.save(savedNotification);
        }
        
        return mapToResponseDto(savedNotification);
    }
    
    @Transactional
    public void sendPendingNotifications() {
        List<Notification> pendingNotifications = notificationRepository.findBySentFalse();
        
        pendingNotifications.forEach(notification -> {
            try {
                sendNotification(notification);
                
                notification.setSent(true);
                notification.setSentAt(LocalDateTime.now());
                notification.setErrorMessage(null);
            } catch (Exception e) {
                logger.error("Failed to send notification: {}", e.getMessage(), e);
                
                notification.setErrorMessage(e.getMessage());
            }
            
            notificationRepository.save(notification);
        });
    }
    
    private void sendNotification(Notification notification) {
        switch (notification.getType()) {
            case EMAIL:
                emailService.sendEmail(notification);
                break;
            case SMS:
                smsService.sendSms(notification);
                break;
            case PUSH:
                pushNotificationService.sendPushNotification(notification);
                break;
            default:
                throw new IllegalArgumentException("Unsupported notification type: " + notification.getType());
        }
    }
    
    private NotificationResponseDto mapToResponseDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .subject(notification.getSubject())
                .content(notification.getContent())
                .type(notification.getType())
                .recipient(notification.getRecipient())
                .sent(notification.getSent())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .errorMessage(notification.getErrorMessage())
                .build();
    }
}

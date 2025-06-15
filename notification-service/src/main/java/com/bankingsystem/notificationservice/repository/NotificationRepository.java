package com.bankingsystem.notificationservice.repository;

import com.bankingsystem.notificationservice.model.Notification;
import com.bankingsystem.notificationservice.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    
    List<Notification> findBySentFalse();
    
    Page<Notification> findByType(NotificationType type, Pageable pageable);
    
    Page<Notification> findByUserIdAndType(Long userId, NotificationType type, Pageable pageable);
    
    Page<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}

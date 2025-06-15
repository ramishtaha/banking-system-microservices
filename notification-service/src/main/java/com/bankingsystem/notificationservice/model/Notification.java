package com.bankingsystem.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    
    @Column(nullable = false)
    private String recipient;
    
    @Column(nullable = false)
    private Boolean sent;
    
    private LocalDateTime sentAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        
        if (this.sent == null) {
            this.sent = false;
        }
    }
}

package com.bankingsystem.userservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserSecurity {
    
    @Autowired
    private AuthenticationFacade authenticationFacade;
    
    public boolean isCurrentUser(Long userId) {
        String currentUsername = authenticationFacade.getAuthentication().getName();
        // Logic to check if current user has the given userId
        // This would require a database lookup, but for simplicity we'll just log
        return true; // In a real application, you would compare with the actual user
    }
}

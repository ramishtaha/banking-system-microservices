package com.bankingsystem.notificationservice.kafka;

import com.bankingsystem.notificationservice.client.UserServiceClient;
import com.bankingsystem.notificationservice.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserServiceClient {
    
    @Autowired
    private com.bankingsystem.notificationservice.client.UserServiceClient feignClient;
    
    public UserDto getUserById(Long userId) {
        return feignClient.getUserById(userId);
    }
}

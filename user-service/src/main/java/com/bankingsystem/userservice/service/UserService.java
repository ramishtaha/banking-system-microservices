package com.bankingsystem.userservice.service;

import com.bankingsystem.userservice.dto.UserRegistrationRequest;
import com.bankingsystem.userservice.dto.UserResponseDto;
import com.bankingsystem.userservice.model.User;
import com.bankingsystem.userservice.model.UserRole;
import com.bankingsystem.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
    
    public Optional<UserResponseDto> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::mapToResponseDto);
    }
    
    public Optional<UserResponseDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToResponseDto);
    }
    
    @Transactional
    public UserResponseDto registerUser(UserRegistrationRequest registrationRequest) {
        // Check if username exists
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }
        
        // Check if email exists
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Create new user
        User user = User.builder()
                .username(registrationRequest.getUsername())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .firstName(registrationRequest.getFirstName())
                .lastName(registrationRequest.getLastName())
                .email(registrationRequest.getEmail())
                .phoneNumber(registrationRequest.getPhoneNumber())
                .address(registrationRequest.getAddress())
                .createdAt(LocalDateTime.now())
                .roles(registrationRequest.getRoles() != null && !registrationRequest.getRoles().isEmpty() ?
                        registrationRequest.getRoles() : Collections.singleton(UserRole.ROLE_USER))
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Send user created event to Kafka
        kafkaTemplate.send("user-events", "user-created", savedUser.getId().toString());
        
        return mapToResponseDto(savedUser);
    }
    
    @Transactional
    public Optional<UserResponseDto> updateUser(Long userId, UserRegistrationRequest updateRequest) {
        return userRepository.findById(userId)
                .map(user -> {
                    // Update user fields
                    user.setFirstName(updateRequest.getFirstName());
                    user.setLastName(updateRequest.getLastName());
                    
                    if (updateRequest.getPhoneNumber() != null) {
                        user.setPhoneNumber(updateRequest.getPhoneNumber());
                    }
                    
                    if (updateRequest.getAddress() != null) {
                        user.setAddress(updateRequest.getAddress());
                    }
                    
                    user.setUpdatedAt(LocalDateTime.now());
                    User updatedUser = userRepository.save(user);
                    
                    // Send user updated event to Kafka
                    kafkaTemplate.send("user-events", "user-updated", user.getId().toString());
                    
                    return mapToResponseDto(updatedUser);
                });
    }
    
    @Transactional
    public void deleteUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            userRepository.delete(user);
            
            // Send user deleted event to Kafka
            kafkaTemplate.send("user-events", "user-deleted", userId.toString());
        });
    }
    
    private UserResponseDto mapToResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .build();
    }
}

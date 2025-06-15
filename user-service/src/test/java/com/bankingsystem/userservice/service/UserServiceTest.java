package com.bankingsystem.userservice.service;

import com.bankingsystem.userservice.dto.UserRegistrationRequest;
import com.bankingsystem.userservice.dto.UserResponseDto;
import com.bankingsystem.userservice.model.User;
import com.bankingsystem.userservice.model.UserRole;
import com.bankingsystem.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedpassword")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .phoneNumber("1234567890")
                .address("Test Address")
                .createdAt(LocalDateTime.now())
                .roles(Collections.singleton(UserRole.ROLE_USER))
                .build();

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("newuser");
        registrationRequest.setPassword("password");
        registrationRequest.setFirstName("New");
        registrationRequest.setLastName("User");
        registrationRequest.setEmail("new@example.com");
        registrationRequest.setPhoneNumber("0987654321");
        registrationRequest.setAddress("New Address");
        registrationRequest.setRoles(Collections.singleton(UserRole.ROLE_USER));
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserResponseDto> result = userService.getAllUsers();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
        assertEquals(testUser.getUsername(), result.get(0).getUsername());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserResponseDto> result = userService.getUserById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnEmpty() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<UserResponseDto> result = userService.getUserById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(999L);
    }

    @Test
    void getUserByUsername_whenUserExists_shouldReturnUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserResponseDto> result = userService.getUserByUsername("testuser");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_whenUserDoesNotExist_shouldReturnEmpty() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<UserResponseDto> result = userService.getUserByUsername("nonexistent");

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void registerUser_whenUsernameAndEmailAreUnique_shouldRegisterUser() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedpassword");
        
        User newUser = User.builder()
                .id(2L)
                .username("newuser")
                .password("encodedpassword")
                .firstName("New")
                .lastName("User")
                .email("new@example.com")
                .phoneNumber("0987654321")
                .address("New Address")
                .createdAt(LocalDateTime.now())
                .roles(Collections.singleton(UserRole.ROLE_USER))
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        UserResponseDto result = userService.registerUser(registrationRequest);

        // Assert
        assertNotNull(result);
        assertEquals(newUser.getId(), result.getId());
        assertEquals(newUser.getUsername(), result.getUsername());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(eq("user-events"), eq("user-created"), anyString());
    }

    @Test
    void registerUser_whenUsernameExists_shouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            userService.registerUser(registrationRequest)
        );
        
        assertEquals("Username is already taken!", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository, never()).save(any(User.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void registerUser_whenEmailExists_shouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            userService.registerUser(registrationRequest)
        );
        
        assertEquals("Email is already in use!", exception.getMessage());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void updateUser_whenUserExists_shouldUpdateUserAndReturnIt() {
        // Arrange
        UserRegistrationRequest updateRequest = new UserRegistrationRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setPhoneNumber("5555555555");
        updateRequest.setAddress("Updated Address");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<UserResponseDto> result = userService.updateUser(1L, updateRequest);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated", result.get().getFirstName());
        assertEquals("Name", result.get().getLastName());
        assertEquals("5555555555", result.get().getPhoneNumber());
        assertEquals("Updated Address", result.get().getAddress());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(eq("user-events"), eq("user-updated"), anyString());
    }

    @Test
    void updateUser_whenUserDoesNotExist_shouldReturnEmpty() {
        // Arrange
        UserRegistrationRequest updateRequest = new UserRegistrationRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<UserResponseDto> result = userService.updateUser(999L, updateRequest);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void deleteUser_whenUserExists_shouldDeleteUserAndSendEvent() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
        verify(kafkaTemplate).send(eq("user-events"), eq("user-deleted"), eq("1"));
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldDoNothing() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        userService.deleteUser(999L);

        // Assert
        verify(userRepository).findById(999L);
        verify(userRepository, never()).delete(any(User.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }
}

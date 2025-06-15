package com.bankingsystem.userservice.controller;

import com.bankingsystem.userservice.dto.UserRegistrationRequest;
import com.bankingsystem.userservice.dto.UserResponseDto;
import com.bankingsystem.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserResponseDto testUserDto;
    private UserRegistrationRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUserDto = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .phoneNumber("1234567890")
                .address("Test Address")
                .build();

        updateRequest = new UserRegistrationRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("Name");
        updateRequest.setPhoneNumber("5555555555");
        updateRequest.setAddress("Updated Address");
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Arrange
        List<UserResponseDto> users = Arrays.asList(testUserDto);
        when(userService.getAllUsers()).thenReturn(users);

        // Act
        ResponseEntity<List<UserResponseDto>> response = userController.getAllUsers();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testUserDto.getId(), response.getBody().get(0).getId());
    }

    @Test
    void getUserById_whenUserExists_shouldReturnUser() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUserDto));

        // Act
        ResponseEntity<UserResponseDto> response = userController.getUserById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUserDto.getId(), response.getBody().getId());
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnNotFound() {
        // Arrange
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserResponseDto> response = userController.getUserById(999L);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getUserByUsername_whenUserExists_shouldReturnUser() {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUserDto));

        // Act
        ResponseEntity<UserResponseDto> response = userController.getUserByUsername("testuser");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testUserDto.getId(), response.getBody().getId());
    }

    @Test
    void getUserByUsername_whenUserDoesNotExist_shouldReturnNotFound() {
        // Arrange
        when(userService.getUserByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserResponseDto> response = userController.getUserByUsername("nonexistent");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateUser_whenUserExists_shouldReturnUpdatedUser() {
        // Arrange
        UserResponseDto updatedUserDto = UserResponseDto.builder()
                .id(1L)
                .username("testuser")
                .firstName("Updated")
                .lastName("Name")
                .email("test@example.com")
                .phoneNumber("5555555555")
                .address("Updated Address")
                .build();
        
        when(userService.updateUser(eq(1L), any(UserRegistrationRequest.class))).thenReturn(Optional.of(updatedUserDto));

        // Act
        ResponseEntity<UserResponseDto> response = userController.updateUser(1L, updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedUserDto.getFirstName(), response.getBody().getFirstName());
        assertEquals(updatedUserDto.getLastName(), response.getBody().getLastName());
    }

    @Test
    void updateUser_whenUserDoesNotExist_shouldReturnNotFound() {
        // Arrange
        when(userService.updateUser(eq(999L), any(UserRegistrationRequest.class))).thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserResponseDto> response = userController.updateUser(999L, updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void deleteUser_shouldReturnNoContent() {
        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser(1L);
    }
}

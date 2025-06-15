package com.bankingsystem.userservice.controller;

import com.bankingsystem.userservice.config.JwtUtils;
import com.bankingsystem.userservice.dto.JwtResponse;
import com.bankingsystem.userservice.dto.LoginRequest;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private UserRegistrationRequest registrationRequest;
    private Authentication authentication;
    private UserDetails userDetails;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");
        
        // Setup user details
        userDetails = new User(
            "testuser", 
            "encodedpassword", 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

    // Setup authentication
    authentication = mock(Authentication.class);
    lenient().when(authentication.getPrincipal()).thenReturn(userDetails);

        // Setup registration request
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("newuser");
        registrationRequest.setPassword("password");
        registrationRequest.setFirstName("New");
        registrationRequest.setLastName("User");
        registrationRequest.setEmail("new@example.com");
        registrationRequest.setPhoneNumber("1234567890");
        registrationRequest.setAddress("Test Address");
        
        // Setup user response dto
        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .username("newuser")
                .firstName("New")
                .lastName("User")
                .email("new@example.com")
                .phoneNumber("1234567890")
                .address("Test Address")
                .build();
    }

    @Test
    void authenticateUser_shouldReturnJwtToken() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("testjwttoken");

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(JwtResponse.class, response.getBody());
        
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals("testjwttoken", jwtResponse.getToken());
        assertEquals("Bearer", jwtResponse.getType());
        assertEquals("testuser", jwtResponse.getUsername());
    }

    @Test
    void registerUser_whenSuccessful_shouldReturnCreatedUser() {
        // Arrange
        when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(userResponseDto);

        // Act
        ResponseEntity<?> response = authController.registerUser(registrationRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(UserResponseDto.class, response.getBody());
        
        UserResponseDto responseDto = (UserResponseDto) response.getBody();
        assertEquals(userResponseDto.getId(), responseDto.getId());
        assertEquals(userResponseDto.getUsername(), responseDto.getUsername());
    }

    @Test
    void registerUser_whenUsernameTaken_shouldReturnBadRequest() {
        // Arrange
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new RuntimeException("Username is already taken!"));

        // Act
        ResponseEntity<?> response = authController.registerUser(registrationRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username is already taken!", response.getBody());
    }

    @Test
    void registerUser_whenEmailTaken_shouldReturnBadRequest() {
        // Arrange
        when(userService.registerUser(any(UserRegistrationRequest.class)))
                .thenThrow(new RuntimeException("Email is already in use!"));

        // Act
        ResponseEntity<?> response = authController.registerUser(registrationRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email is already in use!", response.getBody());
    }
}

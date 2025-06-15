package com.bankingsystem.userservice.service;

import com.bankingsystem.userservice.model.User;
import com.bankingsystem.userservice.model.UserRole;
import com.bankingsystem.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedpassword")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .createdAt(LocalDateTime.now())
                .roles(Collections.singleton(UserRole.ROLE_USER))
                .build();
    }

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedpassword", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertTrue(authorities.contains("ROLE_USER"));
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> 
            userDetailsService.loadUserByUsername("nonexistent")
        );
        
        assertEquals("User not found with username: nonexistent", exception.getMessage());
    }

    @Test
    void loadUserByUsername_withMultipleRoles_shouldReturnAllAuthorities() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .username("admin")
                .password("adminpassword")
                .firstName("Admin")
                .lastName("User")
                .email("admin@example.com")
                .createdAt(LocalDateTime.now())
                .roles(Set.of(UserRole.ROLE_USER, UserRole.ROLE_ADMIN))
                .build();
        
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // Assert
        assertNotNull(userDetails);
        assertEquals(2, userDetails.getAuthorities().size());
        
        Set<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("ROLE_ADMIN"));
    }
}

package com.asan.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.asan.auth.dto.LoginRequest;
import com.asan.auth.dto.RegisterRequest;
import com.asan.auth.dto.UserRegisteredEvent;
import com.asan.auth.entity.User;
import com.asan.auth.messaging.UserEventPublisher;
import com.asan.auth.repository.UserRepository;
import com.asan.auth.security.JwtTokenService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final String TEST_SECRET = "only-for-tests-not-a-production-secret-value";

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserEventPublisher userEventPublisher;

  private AuthService authService;

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
  private final JwtTokenService tokenService = new JwtTokenService(TEST_SECRET, 3_600_000);

  @Test
  void registrationHashesPasswordSetsCustomerRoleAndPublishesEvent() {
    authService = new AuthService(userRepository, passwordEncoder, tokenService, userEventPublisher);
    RegisterRequest request = registerRequest("Siti", "siti@example.test");
    when(userRepository.findByUsername("siti")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("siti@example.test")).thenReturn(Optional.empty());
    when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(7L);
      return user;
    });

    var response = authService.register(request);

    assertEquals("pelanggan", response.getRole());
    assertEquals("siti", response.getUsername());
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).saveAndFlush(userCaptor.capture());
    User savedUser = userCaptor.getValue();
    assertEquals("pelanggan", savedUser.getRole());
    assertTrue(passwordEncoder.matches("a-long-demo-password", savedUser.getPassword()));
    assertNotEquals("a-long-demo-password", savedUser.getPassword());

    ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
    verify(userEventPublisher).publishUserRegistered(eventCaptor.capture());
    assertEquals(7L, eventCaptor.getValue().getUserId());
    assertEquals("siti@example.test", eventCaptor.getValue().getEmail());
  }

  @Test
  void registrationRejectsDuplicateUsername() {
    authService = new AuthService(userRepository, passwordEncoder, tokenService, userEventPublisher);
    RegisterRequest request = registerRequest("Siti", "siti@example.test");
    when(userRepository.findByUsername("siti")).thenReturn(Optional.of(
        new User("siti", "encoded", "siti@example.test", "pelanggan")));

    ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> authService.register(request));

    assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
    verify(userRepository, never()).saveAndFlush(any(User.class));
    verify(userEventPublisher, never()).publishUserRegistered(any());
  }

  @Test
  void loginReturnsSameUnauthorizedResponseForUnknownUserAndWrongPassword() {
    authService = new AuthService(userRepository, passwordEncoder, tokenService, userEventPublisher);
    LoginRequest request = loginRequest("unknown", "long-password-value");
    when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

    ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> authService.login(request));

    assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    assertEquals("Username atau password salah", error.getReason());

    when(userRepository.findByUsername(eq("unknown"))).thenReturn(Optional.of(
        new User("unknown", passwordEncoder.encode("the-correct-password"), "unknown@example.test", "pelanggan")));
    ResponseStatusException wrongPassword = assertThrows(ResponseStatusException.class, () -> authService.login(request));
    assertEquals(error.getStatusCode(), wrongPassword.getStatusCode());
    assertEquals(error.getReason(), wrongPassword.getReason());
  }

  private RegisterRequest registerRequest(String username, String email) {
    RegisterRequest request = new RegisterRequest();
    request.setUsername(username);
    request.setPassword("a-long-demo-password");
    request.setEmail(email);
    request.setNama("Siti");
    request.setAlamat("Jl. Contoh");
    request.setJenisKelamin("PEREMPUAN");
    return request;
  }

  private LoginRequest loginRequest(String username, String password) {
    LoginRequest request = new LoginRequest();
    request.setUsername(username);
    request.setPassword(password);
    return request;
  }
}

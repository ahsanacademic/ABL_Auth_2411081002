package com.asan.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.asan.auth.dto.AuthResponse;
import com.asan.auth.dto.LoginRequest;
import com.asan.auth.dto.RegisterRequest;
import com.asan.auth.dto.UserRegisteredEvent;
import com.asan.auth.entity.User;
import com.asan.auth.messaging.UserEventPublisher;
import com.asan.auth.repository.UserRepository;
import com.asan.auth.security.JwtTokenService;

@Service
public class AuthService {

  private static final String CUSTOMER_ROLE = "pelanggan";
  private static final String INVALID_CREDENTIALS = "Username atau password salah";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenService jwtTokenService;
  private final UserEventPublisher userEventPublisher;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
      JwtTokenService jwtTokenService, UserEventPublisher userEventPublisher) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
    this.userEventPublisher = userEventPublisher;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String username = normalize(request.getUsername());
    String email = normalize(request.getEmail());
    ensureBcryptCompatible(request.getPassword());

    if (userRepository.findByUsername(username).isPresent() || userRepository.findByEmail(email).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username atau email sudah digunakan");
    }

    User user = new User(username, passwordEncoder.encode(request.getPassword()), email, CUSTOMER_ROLE);
    try {
      user = userRepository.saveAndFlush(user);
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username atau email sudah digunakan");
    }

    userEventPublisher.publishUserRegistered(new UserRegisteredEvent(
        user.getId(), username, email, request.getNama().trim(), request.getAlamat().trim(), request.getJenisKelamin()));

    return toAuthResponse(user);
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest request) {
    String username = normalize(request.getUsername());
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS));

    if (!isBcryptCompatible(request.getPassword()) || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
    }

    return toAuthResponse(user);
  }

  private AuthResponse toAuthResponse(User user) {
    return new AuthResponse(jwtTokenService.generateToken(user), user.getUsername(), user.getEmail(), user.getRole());
  }

  private String normalize(String value) {
    return value.trim().toLowerCase(Locale.ROOT);
  }

  private void ensureBcryptCompatible(String password) {
    if (!isBcryptCompatible(password)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password melebihi batas dukungan BCrypt");
    }
  }

  private boolean isBcryptCompatible(String password) {
    return password.getBytes(StandardCharsets.UTF_8).length <= 72;
  }
}

package com.asan.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.asan.auth.entity.User;
import com.asan.auth.repository.UserRepository;

@Configuration
public class AdminBootstrap {

  @Bean
  ApplicationRunner createConfiguredAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder,
      @Value("${app.admin.username:}") String username,
      @Value("${app.admin.password:}") String password,
      @Value("${app.admin.email:}") String email) {
    return args -> {
      boolean hasUsername = !username.isBlank();
      boolean hasPassword = !password.isBlank();
      boolean hasEmail = !email.isBlank();

      if (!hasUsername && !hasPassword && !hasEmail) {
        return;
      }
      if (!hasUsername || !hasPassword || !hasEmail) {
        throw new IllegalStateException("Configure APP_ADMIN_USERNAME, APP_ADMIN_PASSWORD, and APP_ADMIN_EMAIL together");
      }

      if (userRepository.findByUsername(username.trim().toLowerCase()).isEmpty()) {
        userRepository.save(new User(username.trim().toLowerCase(), passwordEncoder.encode(password),
            email.trim().toLowerCase(), "admin"));
      }
    };
  }
}

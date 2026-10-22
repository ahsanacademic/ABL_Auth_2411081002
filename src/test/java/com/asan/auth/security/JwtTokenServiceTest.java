package com.asan.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.asan.auth.entity.User;

import io.jsonwebtoken.JwtException;

class JwtTokenServiceTest {

  private static final String TEST_SECRET = "only-for-tests-not-a-production-secret-value";

  @Test
  void generatedTokenContainsIdentityAndRoleClaims() {
    JwtTokenService tokens = new JwtTokenService(TEST_SECRET, 3_600_000);
    User user = new User("siti", "irrelevant", "siti@example.test", "pelanggan");
    user.setId(42L);

    var claims = tokens.validateToken(tokens.generateToken(user));

    assertEquals("siti", claims.getSubject());
    assertEquals(42, ((Number) claims.get("userId")).intValue());
    assertEquals("pelanggan", claims.get("role", String.class));
    assertEquals("abl-auth", claims.getIssuer());
  }

  @Test
  void rejectsShortSigningSecrets() {
    assertThrows(IllegalArgumentException.class, () -> new JwtTokenService("too-short", 3_600_000));
  }

  @Test
  void rejectsTamperedTokens() {
    JwtTokenService tokens = new JwtTokenService(TEST_SECRET, 3_600_000);
    User user = new User("siti", "irrelevant", "siti@example.test", "pelanggan");
    user.setId(42L);
    String token = tokens.generateToken(user);
    String tampered = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");

    assertThrows(JwtException.class, () -> tokens.validateToken(tampered));
  }
}

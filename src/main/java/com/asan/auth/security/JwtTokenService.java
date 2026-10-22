package com.asan.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.asan.auth.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenService {

  private static final String ISSUER = "abl-auth";
  private static final String AUDIENCE = "abl-services";

  private final SecretKey signingKey;
  private final long expirationMs;

  public JwtTokenService(@Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-ms:3600000}") long expirationMs) {
    byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (secretBytes.length < 32) {
      throw new IllegalArgumentException("app.jwt.secret must contain at least 32 UTF-8 bytes");
    }
    if (expirationMs < 60_000 || expirationMs > 3_600_000) {
      throw new IllegalArgumentException("app.jwt.expiration-ms must be between 60000 and 3600000");
    }
    this.signingKey = Keys.hmacShaKeyFor(secretBytes);
    this.expirationMs = expirationMs;
  }

  public String generateToken(User user) {
    Instant issuedAt = Instant.now();
    return Jwts.builder()
        .issuer(ISSUER)
        .audience().add(AUDIENCE).and()
        .subject(user.getUsername())
        .claim("userId", user.getId())
        .claim("role", user.getRole())
        .claim("email", user.getEmail())
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(issuedAt.plusMillis(expirationMs)))
        .signWith(signingKey)
        .compact();
  }

  public Claims validateToken(String token) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .requireIssuer(ISSUER)
        .requireAudience(AUDIENCE)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}

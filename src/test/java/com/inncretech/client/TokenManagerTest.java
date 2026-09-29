package com.inncretech.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.inncretech.client.auth.TokenManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TokenManagerTest {

  private TokenManager tokenManager;

  @BeforeEach
  void setUp() {
    tokenManager = new TokenManager();
  }

  @Test
  void testUpdateTokensAndValidity() {
    assertFalse(tokenManager.isTokenValid());

    tokenManager.updateTokens("access-123", "refresh-456", 3600L);

    assertTrue(tokenManager.isTokenValid());
    assertEquals("access-123", tokenManager.getAccessToken());
    assertEquals("refresh-456", tokenManager.getRefreshToken());
  }

  @Test
  void testClearTokens() {
    tokenManager.updateTokens("access-123", "refresh-456", 3600L);
    assertTrue(tokenManager.isTokenValid());

    tokenManager.clear();
    assertFalse(tokenManager.isTokenValid());
  }
}

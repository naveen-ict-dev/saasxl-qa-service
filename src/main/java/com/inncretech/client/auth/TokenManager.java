package com.inncretech.client.auth;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TokenManager {

  private final AtomicReference<TokenHolder> tokenRef = new AtomicReference<>();

  public void updateTokens(String accessToken, String refreshToken, Long expiresInSeconds) {
    long validity = (expiresInSeconds != null && expiresInSeconds > 0) ? expiresInSeconds : 3600;
    // Buffer expiration by 60 seconds to avoid edge-of-expiry race conditions
    Instant expiresAt = Instant.now().plusSeconds(Math.max(validity - 60, 30));
    TokenHolder holder = new TokenHolder(accessToken, refreshToken, expiresAt);
    tokenRef.set(holder);
    log.info("Access token updated. Valid until: {}", expiresAt);
  }

  public String getAccessToken() {
    TokenHolder holder = tokenRef.get();
    return holder != null ? holder.accessToken() : null;
  }

  public String getRefreshToken() {
    TokenHolder holder = tokenRef.get();
    return holder != null ? holder.refreshToken() : null;
  }

  public boolean isTokenValid() {
    TokenHolder holder = tokenRef.get();
    if (holder == null || holder.accessToken() == null) {
      return false;
    }
    return Instant.now().isBefore(holder.expiresAt());
  }

  public void clear() {
    tokenRef.set(null);
  }

  private record TokenHolder(String accessToken, String refreshToken, Instant expiresAt) {}
}

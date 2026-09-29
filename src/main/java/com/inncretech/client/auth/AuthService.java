package com.inncretech.client.auth;

import com.inncretech.client.model.dto.AuthResponseDTO;
import com.inncretech.client.model.dto.LoginRequestDTO;
import com.inncretech.client.model.dto.RefreshTokenRequestDTO;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AuthService {

  private final WebClient rawWebClient;
  private final TokenManager tokenManager;

  @Value("${backend.auth.email}")
  private String email;

  @Value("${backend.auth.password}")
  private String password;

  @Value("${backend.auth.login-path:/idp/api/v1/user/v2/login}")
  private String loginPath;

  @Value("${backend.auth.refresh-path:/idp/api/v1/user/v2/refresh}")
  private String refreshPath;

  @Value("${backend.auth.token-path:/idp/api/v1/user/token}")
  private String fallbackTokenPath;

  public AuthService(
      @Value("${backend.base-url}") String baseUrl,
      TokenManager tokenManager) {
    this.rawWebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
    this.tokenManager = tokenManager;
  }

  /**
   * Returns a valid access token. If current token is missing or expired,
   * performs login or token refresh automatically.
   */
  public synchronized String getOrRefreshAccessToken() {
    if (tokenManager.isTokenValid()) {
      return tokenManager.getAccessToken();
    }

    if (tokenManager.getRefreshToken() != null) {
      try {
        log.info("Attempting to refresh access token using refresh token...");
        refreshAccessToken();
        return tokenManager.getAccessToken();
      } catch (Exception ex) {
        log.warn("Token refresh failed. Falling back to full login", ex);
      }
    }

    log.info("Performing login for user: {}", email);
    login();
    return tokenManager.getAccessToken();
  }

  /**
   * Calls POST /idp/api/v1/user/v2/login to authenticate and retrieve access & refresh tokens.
   */
  public void login() {
    LoginRequestDTO request = LoginRequestDTO.builder()
        .email(email)
        .password(password)
        .build();

    try {
      AuthResponseDTO authResponse = rawWebClient.post()
          .uri(loginPath)
          .bodyValue(request)
          .retrieve()
          .bodyToMono(AuthResponseDTO.class)
          .block();

      if (authResponse != null && authResponse.getAccessToken() != null) {
        tokenManager.updateTokens(
            authResponse.getAccessToken(),
            authResponse.getRefreshToken(),
            authResponse.getAccessTokenExpiresIn());
        log.info("Successfully logged in user ID: {}",
            authResponse.getUser() != null ? authResponse.getUser().getId() : "N/A");
      } else {
        throw new IllegalStateException("Login response did not contain an access token");
      }
    } catch (WebClientResponseException ex) {
      log.warn("v2/login failed with status {}. Attempting fallback to legacy /token endpoint...", ex.getStatusCode());
      loginFallback(request);
    }
  }

  /**
   * Fallback for older environments: POST /idp/api/v1/user/token
   */
  private void loginFallback(LoginRequestDTO request) {
    Map<?, ?> response = rawWebClient.post()
        .uri(fallbackTokenPath)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Map.class)
        .block();

    if (response != null && response.containsKey("token")) {
      String token = (String) response.get("token");
      tokenManager.updateTokens(token, null, 3600L);
      log.info("Successfully retrieved token via fallback endpoint");
    } else {
      throw new IllegalStateException("Failed to obtain token from fallback endpoint: " + response);
    }
  }

  /**
   * Calls POST /idp/api/v1/user/v2/refresh to rotate the tokens.
   */
  public void refreshAccessToken() {
    String refreshToken = tokenManager.getRefreshToken();
    if (refreshToken == null) {
      throw new IllegalStateException("No refresh token available");
    }

    RefreshTokenRequestDTO request = RefreshTokenRequestDTO.builder()
        .refreshToken(refreshToken)
        .build();

    Map<?, ?> response = rawWebClient.post()
        .uri(refreshPath)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(Map.class)
        .block();

    if (response != null && response.containsKey("accessToken")) {
      String newAccessToken = (String) response.get("accessToken");
      Object refreshObj = response.get("refreshToken");
      String newRefreshToken = refreshObj != null ? refreshObj.toString() : refreshToken;
      tokenManager.updateTokens(newAccessToken, newRefreshToken, 3600L);
      log.info("Token refreshed successfully");
    } else {
      throw new IllegalStateException("Refresh response did not contain access token: " + response);
    }
  }
}

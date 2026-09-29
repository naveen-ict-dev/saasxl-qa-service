package com.inncretech.client.client;

import com.inncretech.client.model.dto.AuthResponseDTO;
import com.inncretech.client.model.dto.LoginRequestDTO;
import com.inncretech.client.model.dto.RefreshTokenRequestDTO;
import com.inncretech.client.model.dto.SignUpRequestDTO;
import com.inncretech.client.model.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthApiClient {

  private static final String BEARER_PREFIX = "Bearer ";

  private final WebClient backendWebClient;

  @Value("${backend.auth.automation-token:a9180787-388e-4f93-8473-87c56b9a22aa}")
  private String automationToken;

  @Value("${backend.auth.login-path-v1:/idp/api/v1/user/login}")
  private String loginPathV1;

  @Value("${backend.auth.login-path:/idp/api/v1/user/v2/login}")
  private String loginPathV2;

  /**
   * Calls POST /idp/api/v1/user/login (permitAll — no Bearer token attached, per
   * WebClientConfig's authHeaderFilter whitelist).
   */
  public Mono<UserDTO> login(LoginRequestDTO request) {
    return backendWebClient.post()
        .uri(loginPathV1)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnError(err -> log.error("Login failed for {}", request.getEmail(), err));
  }

  /**
   * Calls POST /idp/api/v1/user/login and returns the Bearer token carried on the
   * Authorization response header, since this endpoint puts it there rather than in the body.
   */
  public Mono<String> loginAndCaptureToken(LoginRequestDTO request) {
    return backendWebClient.post()
        .uri(loginPathV1)
        .bodyValue(request)
        .retrieve()
        .toEntity(UserDTO.class)
        .map(response -> {
          String authHeader = response.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
          if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new IllegalStateException("No Bearer token found in the Authorization header.");
          }
          return authHeader.substring(BEARER_PREFIX.length());
        })
        .doOnError(err -> log.error("Login failed for {}", request.getEmail(), err));
  }

  /**
   * Calls POST /idp/api/v1/user/v2/login (permitAll), returning both an access and refresh token
   * in the JSON body — unlike the v1 login this suite's bootstrap uses.
   */
  public Mono<AuthResponseDTO> loginV2(LoginRequestDTO request) {
    return backendWebClient.post()
        .uri(loginPathV2)
        .bodyValue(request)
        .retrieve()
        .bodyToMono(AuthResponseDTO.class)
        .doOnError(err -> log.error("v2 login failed for {}", request.getEmail(), err));
  }

  /**
   * Calls POST /idp/api/v1/user/v2/refresh (permitAll) to rotate an access/refresh token pair.
   */
  public Mono<AuthResponseDTO> refreshToken(RefreshTokenRequestDTO request) {
    return backendWebClient.post()
        .uri("/idp/api/v1/user/v2/refresh")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(AuthResponseDTO.class)
        .doOnError(err -> log.error("Token refresh failed", err));
  }

  /**
   * Calls POST /idp/api/v1/user/sign-up (permitAll) to create a new company and user.
   */
  public Mono<UserDTO> signUp(SignUpRequestDTO request) {
    return backendWebClient.post()
        .uri("/idp/api/v1/user/sign-up")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnError(err -> log.error("Sign-up failed for {}", request.getEmail(), err));
  }

  /**
   * Calls GET /idp/api/v1/user/automation/getVerificationHash — an automation-only endpoint that
   * returns the email-verification hash directly, bypassing the real signup email.
   */
  public Mono<String> getVerificationHash(String email) {
    return backendWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/idp/api/v1/user/automation/getVerificationHash")
            .queryParam("automationToken", automationToken)
            .queryParam("email", email)
            .build())
        .retrieve()
        .bodyToMono(String.class)
        .doOnError(err -> log.error("Failed to fetch verification hash for {}", email, err));
  }

  /**
   * Calls GET /idp/api/v1/user/verify to activate the account created by signUp.
   */
  public Mono<UserDTO> verifyEmail(String hash) {
    return backendWebClient.get()
        .uri(uriBuilder -> uriBuilder.path("/idp/api/v1/user/verify").queryParam("hash", hash).build())
        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnError(err -> log.error("Email verification failed for hash {}", hash, err));
  }

  /**
   * Calls GET /idp/api/v1/user/automation/changeUserRole — an automation-only endpoint that
   * promotes the given user to ACCOUNT_ADMIN/LAKEHOUSE_ADMIN.
   */
  public Mono<String> promoteToAdmin(String email) {
    return backendWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/idp/api/v1/user/automation/changeUserRole")
            .queryParam("automationToken", automationToken)
            .queryParam("email", email)
            .build())
        .retrieve()
        .bodyToMono(String.class)
        .doOnError(err -> log.error("Failed to promote {} to admin", email, err));
  }
}

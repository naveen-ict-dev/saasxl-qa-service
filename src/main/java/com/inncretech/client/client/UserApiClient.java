package com.inncretech.client.client;

import com.inncretech.client.model.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /idp/api/v1/user using the Bearer token in the header.
   */
  public Mono<UserDTO> getCurrentUser() {
    return backendWebClient.get()
        .uri("/idp/api/v1/user")
        .retrieve()
        .bodyToMono(UserDTO.class)
        .doOnSuccess(user -> log.info("Retrieved current user: id={}, email={}, companyId={}",
            user.getId(), user.getEmail(), user.getCompanyId()))
        .doOnError(err -> log.error("Failed to fetch current user", err));
  }
}

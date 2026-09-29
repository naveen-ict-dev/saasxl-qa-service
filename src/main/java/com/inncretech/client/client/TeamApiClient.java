package com.inncretech.client.client;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class TeamApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /idp/api/v1/team
   */
  public Mono<Map<?, ?>> getTeam() {
    return backendWebClient.get()
        .uri("/idp/api/v1/team")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch team", err));
  }
}

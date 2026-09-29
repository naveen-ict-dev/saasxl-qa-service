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
public class ConnectorApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /connector/api/v1/instance
   */
  public Mono<Map<?, ?>> getInstances() {
    return backendWebClient.get()
        .uri("/connector/api/v1/instance")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch connector instances", err));
  }
}

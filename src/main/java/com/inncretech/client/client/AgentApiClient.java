package com.inncretech.client.client;

import java.util.List;
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
public class AgentApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls GET /agents/api/v1/config/byNames
   */
  public Mono<List<Map<?, ?>>> getConfigsByNames(List<String> names) {
    return backendWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/agents/api/v1/config/byNames")
            .queryParam("name", names)
            .build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch agent configs {}", names, err));
  }

  /**
   * Calls GET /agents/api/v1/templates/byNames
   */
  public Mono<List<Map<?, ?>>> getTemplatesByNames(List<String> names) {
    return backendWebClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/agents/api/v1/templates/byNames")
            .queryParam("name", names)
            .build())
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch agent templates {}", names, err));
  }
}

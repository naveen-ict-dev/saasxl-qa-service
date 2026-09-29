package com.inncretech.client.client;

import com.inncretech.client.model.dto.CustomAgentDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAgentApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /customAgents/api/v1
   */
  public Mono<CustomAgentDTO> create(CustomAgentDTO agent) {
    return backendWebClient.post()
        .uri("/customAgents/api/v1")
        .bodyValue(agent)
        .retrieve()
        .bodyToMono(CustomAgentDTO.class)
        .doOnError(err -> log.error("Failed to create custom agent {}", agent.getName(), err));
  }

  /**
   * Calls GET /customAgents/api/v1
   */
  public Mono<List<CustomAgentDTO>> getAll() {
    return backendWebClient.get()
        .uri("/customAgents/api/v1")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<CustomAgentDTO>>() {})
        .doOnError(err -> log.error("Failed to fetch custom agents", err));
  }
}

package com.inncretech.client.client;

import com.inncretech.client.model.dto.CustomAgentDTO;
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

  /**
   * Calls GET /customAgents/api/v1/{customAgentId}
   */
  public Mono<CustomAgentDTO> getById(String customAgentId) {
    return backendWebClient.get()
        .uri("/customAgents/api/v1/{customAgentId}", customAgentId)
        .retrieve()
        .bodyToMono(CustomAgentDTO.class)
        .doOnError(err -> log.error("Failed to fetch custom agent {}", customAgentId, err));
  }

  /**
   * Calls PUT /customAgents/api/v1/{customAgentId}
   */
  public Mono<CustomAgentDTO> update(String customAgentId, CustomAgentDTO agent) {
    return backendWebClient.put()
        .uri("/customAgents/api/v1/{customAgentId}", customAgentId)
        .bodyValue(agent)
        .retrieve()
        .bodyToMono(CustomAgentDTO.class)
        .doOnError(err -> log.error("Failed to update custom agent {}", customAgentId, err));
  }

  /**
   * Calls DELETE /customAgents/api/v1/{customAgentId}
   */
  public Mono<Void> delete(String customAgentId) {
    return backendWebClient.delete()
        .uri("/customAgents/api/v1/{customAgentId}", customAgentId)
        .retrieve()
        .bodyToMono(Void.class)
        .doOnError(err -> log.error("Failed to delete custom agent {}", customAgentId, err));
  }

  /**
   * Calls POST /customAgents/api/v1/public/byIds
   */
  public Mono<List<CustomAgentDTO>> getActivePublicByIds(List<String> ids) {
    return backendWebClient.post()
        .uri("/customAgents/api/v1/public/byIds")
        .bodyValue(Map.of("ids", ids))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<CustomAgentDTO>>() {})
        .doOnError(err -> log.error("Failed to fetch public custom agents {}", ids, err));
  }

  /**
   * Calls GET /customAgents/api/v1/{customAgentId}/workbooks
   */
  public Mono<Map<?, ?>> getWorkbooks(String customAgentId) {
    return backendWebClient.get()
        .uri("/customAgents/api/v1/{customAgentId}/workbooks", customAgentId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch workbooks for custom agent {}", customAgentId, err));
  }

  /**
   * Calls GET /customAgents/api/v1/{customAgentId}/runs
   */
  public Mono<Map<?, ?>> getRuns(String customAgentId) {
    return backendWebClient.get()
        .uri("/customAgents/api/v1/{customAgentId}/runs", customAgentId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to fetch runs for custom agent {}", customAgentId, err));
  }
}

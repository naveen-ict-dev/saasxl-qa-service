package com.inncretech.client.client;

import com.inncretech.client.model.dto.AgentProfileRequestDTO;
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
public class AgentProfileApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /api/v1/agentProfiles (requires ACCOUNT_ADMIN)
   */
  public Mono<Map<?, ?>> createProfile(AgentProfileRequestDTO request) {
    return backendWebClient.post()
        .uri("/api/v1/agentProfiles")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<?, ?>>() {})
        .doOnError(err -> log.error("Failed to create agent profile {}", request.getName(), err));
  }

  /**
   * Calls GET /api/v1/agentProfiles
   */
  public Mono<List<Map<?, ?>>> getProfiles() {
    return backendWebClient.get()
        .uri("/api/v1/agentProfiles")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<Map<?, ?>>>() {})
        .doOnError(err -> log.error("Failed to fetch agent profiles", err));
  }
}

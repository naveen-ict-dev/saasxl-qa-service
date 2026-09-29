package com.inncretech.client.client;

import com.inncretech.client.model.dto.AgentSkillDTO;
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
public class AgentSkillApiClient {

  private final WebClient backendWebClient;

  /**
   * Calls POST /skills/api/v1
   */
  public Mono<AgentSkillDTO> create(AgentSkillDTO skill) {
    return backendWebClient.post()
        .uri("/skills/api/v1")
        .bodyValue(skill)
        .retrieve()
        .bodyToMono(AgentSkillDTO.class)
        .doOnError(err -> log.error("Failed to create agent skill", err));
  }

  /**
   * Calls GET /skills/api/v1
   */
  public Mono<List<AgentSkillDTO>> list() {
    return backendWebClient.get()
        .uri("/skills/api/v1")
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<AgentSkillDTO>>() {})
        .doOnError(err -> log.error("Failed to list agent skills", err));
  }

  /**
   * Calls GET /skills/api/v1/{skillId}
   */
  public Mono<AgentSkillDTO> getById(Long skillId) {
    return backendWebClient.get()
        .uri("/skills/api/v1/{skillId}", skillId)
        .retrieve()
        .bodyToMono(AgentSkillDTO.class)
        .doOnError(err -> log.error("Failed to fetch agent skill {}", skillId, err));
  }

  /**
   * Calls GET /skills/api/v1/{skillId}/versions
   */
  public Mono<List<?>> getVersions(Long skillId) {
    return backendWebClient.get()
        .uri("/skills/api/v1/{skillId}/versions", skillId)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<List<?>>() {})
        .doOnError(err -> log.error("Failed to fetch versions for agent skill {}", skillId, err));
  }
}
